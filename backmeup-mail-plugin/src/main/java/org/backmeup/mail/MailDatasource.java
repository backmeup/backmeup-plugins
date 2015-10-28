package org.backmeup.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;

import org.backmeup.mail.core.Attachment;
import org.backmeup.mail.core.Content;
import org.backmeup.mail.core.MediaType;
import org.backmeup.mail.core.MessageInfo;
import org.backmeup.mail.core.TextContent;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.Datasource;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

/**
 * The DropboxDatasource is capable of listing all directories and files of a certain directory and of downloading
 * certain files from Dropbox.
 * 
 * @author fschoeppl
 */
public class MailDatasource implements Datasource, Validationable {
    private static final String ENCODING_UTF_8 = "UTF-8";
    private static final String MESSAGE_FOLDER_FORMAT = "org.backmeup.mail.MailDatasource.MESSAGE_FOLDER_FORMAT";
    private static final String MESSAGE_HTML_WRAP = "org.backmeup.mail.MailDatasource.MESSAGE_HTML_WRAP";
    private static final String MESSAGE_HTML_ATTACHMENT_ENTRY = "org.backmeup.mail.MailDatasource.MESSAGE_HTML_ATTACHMENT_ENTRY";
    private static final String MESSAGE_HTML_ATTACHMENT_WRAP = "org.backmeup.mail.MailDatasource.MESSAGE_HTML_ATTACHMENT_WRAP";
    private static final String INDEX_HTML_WRAP = "org.backmeup.mail.MailDatasource.INDEX_HTML_WRAP";
    private static final String INDEX_HTML_ENTRY = "org.backmeup.mail.MailDatasource.INDEX_HTML_ENTRY";

    private final ResourceBundle textBundle = ResourceBundle.getBundle(MailDatasource.class.getSimpleName());
    
    private final SimpleDateFormat folderFormat;

    private final Pattern bodyRegex = Pattern.compile("<body.*?>(.*?)</body>", Pattern.DOTALL);
    private final Pattern headRegex = Pattern.compile("<head.*?>(.*?)</head>", Pattern.DOTALL);
    private final Pattern htmlRegex = Pattern.compile("<html.*?>(.*?)</html>", Pattern.DOTALL);

    public MailDatasource() {
        this.folderFormat = new SimpleDateFormat(this.textBundle.getString(MESSAGE_FOLDER_FORMAT));
    }

    private String getCharset(Part bp) throws MessagingException {
        Pattern pattern = Pattern.compile(".*?charset=(.*?)$");
        Matcher matcher = pattern.matcher(bp.getContentType());
        if (matcher.find()) {
            String result = matcher.group(1).toUpperCase();
            if (result.contains(";")) {
                result = result.split(";")[0];
            }
            return result;
        }
        return ENCODING_UTF_8;
    }

    private TextContent getText(Part part) throws MessagingException, IOException {
        String charset = getCharset(part);
        if (part.isMimeType(MediaType.TEXT)) {
            Object o = part.getContent();
            TextContent tc = new TextContent();
            if (o instanceof String) {
                tc.setHtml(part.isMimeType(MediaType.TEXT_HTML));
                tc.setCharset(charset);
                tc.setText((String) o);
                return tc;
            } else if (o instanceof InputStream) {
                InputStream i = (InputStream) o;
                try {
                    tc.setText(new Scanner(i, getCharset(part)).useDelimiter("\\A").next());
                    tc.setCharset(charset);
                    tc.setHtml(part.isMimeType(MediaType.TEXT_HTML));
                    return tc;
                } catch (NoSuchElementException nee) {
                    return null;
                }
            }
        } else if (part.isMimeType(MediaType.MULTIPART_ALTERNATIVE)) {
            Multipart mp = (Multipart) part.getContent();
            TextContent text = new TextContent();
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType(MediaType.TEXT_PLAIN)) {
                    if (text == null) {
                        text = getText(bp);
                    }
                    continue;
                } else if (bp.isMimeType(MediaType.TEXT_HTML)) {
                    TextContent s = getText(bp);
                    if (s != null) {
                        return s;
                    }
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (part.isMimeType(MediaType.MULTIPART)) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                TextContent s = getText(mp.getBodyPart(i));
                if (s != null) {
                    return s;
                }
            }
        }
        return null;
    }

    public List<Attachment> getAttachments(Part part) throws MessagingException, IOException {
        List<Attachment> attachments = new ArrayList<>();
        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            Attachment a = new Attachment();
            a.setFilename(MimeUtility.decodeText(part.getFileName()));
            a.setStream(part.getInputStream());
            attachments.add(a);
        } else if (part.isMimeType(MediaType.MULTIPART)) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                attachments.addAll(getAttachments(mp.getBodyPart(i)));
            }
        }
        return attachments;
    }

    public List<Part> getNestedMessages(Part p) throws MessagingException, IOException {
        List<Part> nested = new ArrayList<>();
        if (p.isMimeType(MediaType.MULTIPART)) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                nested.addAll(getNestedMessages(mp.getBodyPart(i)));
            }
        } else if (p.isMimeType(MediaType.MESSAGE_RFC822)) {
            nested.add(p);
        }
        return nested;
    }

    private String join(Object[] arr, String pattern) {
        if (arr == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Object element : arr) {
            sb.append(element).append(pattern);
        }

        if (arr.length > 0) {
            sb.delete(sb.length() - pattern.length(), sb.length());
        }
        return sb.toString();
    }

    private void handlePart(Part m, String folderName, Storage storage, Set<String> alreadyInspected,
            List<MessageInfo> indexDetails, Progressable progressor) throws StorageException, MessagingException,
            IOException {

        String from = "N/A";
        String to = "N/A";
        String subject = "N/A";
        String sentAt = "N/A";
        Date receivedAt = new Date();
        Date modified = null;
        int msgNmbr = 0;

        if (m instanceof Message) {
            Message mesg = (Message) m;
            if (mesg.getFrom() != null) {
                from = join(mesg.getFrom(), ", ");
            }

            if (mesg.getRecipients(Message.RecipientType.TO) != null) {
                to = join(mesg.getAllRecipients(), ", ");
            }
            msgNmbr = mesg.getMessageNumber();
            subject = mesg.getSubject();
            sentAt = mesg.getSentDate().toString();
            receivedAt = mesg.getReceivedDate();
            modified = mesg.getReceivedDate() != null ? mesg.getReceivedDate() : mesg.getSentDate();
        }

        String destinationFileName = folderName + "content" + msgNmbr + ".html";

        if (alreadyInspected.contains(destinationFileName)) {
            return;
        }

        TextContent text = getText(m);
        // nothing to do; message is empty
        if (text == null) {
            return;
        }

        String appendToHead = "";
        String appendToBody = "";

        if (text.isHtml()) {
            Matcher matcher = this.headRegex.matcher(text.getText());
            if (matcher.find()) {
                appendToHead = matcher.group(1);
            }
            matcher = this.bodyRegex.matcher(text.getText());
            if (matcher.find()) {
                appendToBody = matcher.group(1);
            } else {
                matcher = this.htmlRegex.matcher(text.getText());
                if (matcher.find()) {
                    appendToBody = matcher.group(1);
                } else {
                    progressor.progress("Couldn't find html element / falling back to content of string");
                    appendToBody = text.getText();
                }
            }
        }

        if (!text.isHtml()) {
            appendToBody = text.getText();
        }

        String attachmentFolder = folderName + "attachments" + msgNmbr + "/";
        List<Attachment> attachments = getAttachments(m);
        StringBuilder attachmentLinks = new StringBuilder();

        for (Attachment a : attachments) {
            attachmentLinks.append(MessageFormat.format(this.textBundle.getString(MESSAGE_HTML_ATTACHMENT_ENTRY),
                    "attachments" + msgNmbr + "/" + a.getFilename(), a.getFilename()));
            storage.addFile(a.getStream(), attachmentFolder + a.getFilename(), new MetainfoContainer());
        }

        // get embedded images
        List<Content> contentIds = getContentIds(m);
        for (Content c : contentIds) {
            appendToBody = appendToBody.replace("cid:" + c.getContentId(), "attachments" + msgNmbr + "/" + c.getFilename());
            storage.addFile(c.getContent(), attachmentFolder + c.getFilename(), new MetainfoContainer());
        }

        String attachmentString = attachmentLinks.length() == 0 ? "" : MessageFormat.format(
                this.textBundle.getString(MESSAGE_HTML_ATTACHMENT_WRAP), attachmentLinks.toString());

        String htmlText = MessageFormat.format(this.textBundle.getString(MESSAGE_HTML_WRAP), appendToHead,
                appendToBody, subject, from, sentAt, receivedAt, to, attachmentString, text.getCharset());

        alreadyInspected.add(destinationFileName);
        indexDetails.add(new MessageInfo(destinationFileName, subject, from, to, sentAt, receivedAt));
        MetainfoContainer infos = new MetainfoContainer();
        Metainfo metaData = new Metainfo();
        metaData.setBackupDate(new Date());
        metaData.setDestination(destinationFileName);
        metaData.setAttribute("from", from);
        metaData.setAttribute("to", to);
        metaData.setAttribute("receivedAt", receivedAt.toString());
        metaData.setAttribute("sentAt", sentAt);
        metaData.setModified(modified);
        metaData.setAttribute("subject", subject);
        infos.addMetainfo(metaData);
        storage.addFile(new ByteArrayInputStream(htmlText.getBytes(text.getCharset())), destinationFileName,
                infos);

        // handle nested messages
        List<Part> nested = getNestedMessages(m);
        for (int i = 0; i < nested.size(); i++) {
            handlePart(nested.get(i), folderName + "/nested/", storage, alreadyInspected, indexDetails, progressor);
        }
    }

    private List<Content> getContentIds(Part m) throws MessagingException, IOException {
        List<Content> contentIds = new ArrayList<>();
        Stack<Part> parts = new Stack<>();
        parts.push(m);
        while (!parts.empty()) {
            Part current = parts.pop();

            // analyze current part
            String[] header = current.getHeader("Content-ID");
            if (header != null && header.length > 0) {
                Content c = new Content();
                c.setContentId(header[0]);
                if (c.getContentId().startsWith("<")) {
                    c.setContentId(c.getContentId().substring(1));
                }
                if (c.getContentId().endsWith(">")) {
                    c.setContentId(c.getContentId().substring(0, c.getContentId().length() - 1));
                }
                c.setFilename(current.getFileName());
                c.setContent((InputStream) current.getDataHandler().getContent());
                contentIds.add(c);
            }

            // push children on stack
            if (current.isMimeType(MediaType.MULTIPART)) {
                Multipart mp = (Multipart) current.getContent();
                int count = mp.getCount();
                for (int i = 0; i < count; i++) {
                    parts.push(mp.getBodyPart(i));
                }
            }
        }
        return contentIds;
    }

    private void handleFolder(Folder folder, Storage storage, Set<String> alreadyInspected,
            List<MessageInfo> indexDetails, int retryCount, Progressable progressor) throws IOException,
            MessagingException, StorageException {
        try {
            progressor.progress("Processing next folder: " + folder.getName());
            folder.open(Folder.READ_ONLY);
            Message[] messages = folder.getMessages();
            double prev = 0;
            for (int i = 0; i < messages.length; i++) {
                String folderName = folder.getFullName() + "/"
                        + this.folderFormat.format(messages[i].getReceivedDate()) + "/";

                handlePart(messages[i], folderName, storage, alreadyInspected, indexDetails, progressor);
                double percent = i * 100 / (double) messages.length;
                if (percent - 10 > prev) {
                    progressor.progress(String.format("%3.2f%%", percent));
                    prev = percent;
                }
            }
            folder.close(false);
        } catch (FolderClosedException fce) {
            progressor.progress("Retrying folder " + folder.toString());
            if (retryCount < 10) {
                handleFolder(folder, storage, alreadyInspected, indexDetails, retryCount + 1, progressor);
            } else {
                throw new PluginException(MailDescriptor.MAIL_ID, "Failed to download folder", fce);
            }
        } catch (MessagingException me) {
            progressor.progress(me.toString());
            progressor.progress(me.getMessage());
        }
    }

    public void handleDownloadAll(Folder current, Map<String, String> accessData, Storage storage, Set<String> alreadyInspected,
            List<MessageInfo> indexDetails, Progressable progressor) throws IOException, MessagingException,
            StorageException {
        if (alreadyInspected.contains(current.getFullName())) {
            return;
        }
        int retryCount = 0;
        handleFolder(current, storage, alreadyInspected, indexDetails, retryCount, progressor);
        alreadyInspected.add(current.getFullName());

        Folder[] subFolders = current.list("*");
        for (Folder sub : subFolders) {
            handleDownloadAll(sub, accessData, storage, alreadyInspected, indexDetails, progressor);
        }
    }

    private void generateIndex(Storage storage, List<MessageInfo> indexDetails) throws UnsupportedEncodingException,
            StorageException {
        StringBuilder sb = new StringBuilder();
        Collections.sort(indexDetails, new Comparator<MessageInfo>() {
            @Override
            public int compare(MessageInfo o1, MessageInfo o2) {
                return o2.getReceivedAt().compareTo(o1.getReceivedAt());
            }
        });

        for (MessageInfo mi : indexDetails) {
            sb.append(MessageFormat.format(this.textBundle.getString(INDEX_HTML_ENTRY), mi.getSubject(), mi.getFrom(),
                    mi.getSentAt(), mi.getReceivedAt(), mi.getTo(), mi.getFileName()));
        }
        String indexHtml = MessageFormat.format(this.textBundle.getString(INDEX_HTML_WRAP), sb.toString());

        storage.addFile(new ByteArrayInputStream(indexHtml.getBytes(ENCODING_UTF_8)), "index.html", new MetainfoContainer());
    }

    @Override
    public void downloadAll(PluginProfileDTO pluginProfile, PluginContext pluginContext, Storage storage,
            Progressable progressor) throws StorageException {
        try {
            Map<String, String> accessData = pluginProfile.getAuthData().getProperties();
            Properties mailProps = new Properties();
            mailProps.putAll(accessData);
            
            Session session = Session.getInstance(mailProps);
            Store store = session.getStore();
            progressor.progress("Connecting to mail provider " + accessData.get(MailAuthenticator.AUTHPROP_MAIL_HOST));
            store.connect(accessData.get(MailAuthenticator.AUTHPROP_MAIL_HOST),
                    accessData.get(MailAuthenticator.AUTHPROP_MAIL_USER),
                    accessData.get(MailAuthenticator.AUTHPROP_MAIL_PASSWORD));
            progressor.progress("Connection successfull");

            Set<String> alreadyInspected = new HashSet<>();

            progressor.progress("Get list of folders");
            Folder[] folders = store.getDefaultFolder().list("*");
            progressor.progress("No. of folders retrieved: " + folders.length);

            List<MessageInfo> indexDetails = new ArrayList<>();
            List<String> options = pluginProfile.getOptions();
            if (!options.isEmpty()) {
                List<Folder> toVisit = new ArrayList<>();
                for (Folder f : folders) {
                    if (options.contains(f.getFullName())) {
                        toVisit.add(f);
                    }
                }

                if (toVisit.isEmpty()) {
                    //enabling download all folders if no options which folders to download are set by UI
                    progressor.progress("adding folders progress upon: " + Arrays.toString(folders));
                } else {
                    //download the selected imap folders
                    folders = toVisit.toArray(new Folder[] {});
                }

            }

            for (Folder folder : folders) {
                handleDownloadAll(folder, accessData, storage, alreadyInspected, indexDetails, progressor);
            }
            progressor.progress("Download completed");
            progressor.progress("Creating index ...");
            // generate index based on message info structs
            generateIndex(storage, indexDetails);
            progressor.progress("Creating index completed");

            store.close();
        } catch (NoSuchProviderException e) {
            progressor.progress(e.toString());
            progressor.progress(e.getMessage());
            throw new PluginException(MailDescriptor.MAIL_ID, "No such provider", e);
        } catch (MessagingException | IOException e) {
            progressor.progress(e.toString());
            progressor.progress(e.getMessage());
            throw new PluginException(MailDescriptor.MAIL_ID, "An error occured during the backup", e);
        } 
    }

    @Override
    public boolean hasRequiredProperties() {
        return false;
    }

    @Override
    public List<RequiredInputField> getRequiredProperties() {
        return null;
    }

    @Override
    public ValidationNotes validateProperties(Map<String, String> properties) {
        return null;
    }

    @Override
    public boolean hasAvailableOptions() {
        return true;
    }

    @Override
    public ValidationNotes validateOptions(List<String> options) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public List<String> getAvailableOptions(Map<String, String> accessData) {
        List<String> options = new ArrayList<>();
        if (accessData == null || accessData.isEmpty()) {
            return options;
        }
        
        try {
            Properties mailProps = new Properties();
            mailProps.putAll(accessData);
            
            Session session = Session.getInstance(mailProps);
            Store store = session.getStore();
            store.connect(accessData.get(MailAuthenticator.AUTHPROP_MAIL_HOST),
                    accessData.get(MailAuthenticator.AUTHPROP_MAIL_USER),
                    accessData.get(MailAuthenticator.AUTHPROP_MAIL_PASSWORD));
            Folder[] folders = store.getDefaultFolder().list("*");
            for (Folder folder : folders) {
                String folderName = folder.getFullName();
                options.add(folderName);
            }

            store.close();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Collections.sort(options);
        return options;
    }
}
