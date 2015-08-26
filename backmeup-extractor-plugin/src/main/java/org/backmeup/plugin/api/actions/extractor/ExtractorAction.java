package org.backmeup.plugin.api.actions.extractor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;

import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.actions.extractor.dao.AppointmentDAO;
import org.backmeup.plugin.api.actions.extractor.dao.PersonIdentityDAO;
import org.backmeup.plugin.api.actions.extractor.model.Appointment;
import org.backmeup.plugin.api.actions.extractor.model.PersonIdentity;
import org.backmeup.plugin.api.connectors.Action;
import org.backmeup.plugin.api.connectors.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.hibernate.SessionFactory;
import org.jsoup.parser.ParseError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ezvcard.Ezvcard;
import ezvcard.Ezvcard.ParserChainTextReader;
import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Nickname;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.Telephone;
import ezvcard.property.Title;


//@Controller
public class ExtractorAction implements Action {
	@Autowired
    private PersonIdentityDAO personIdentityDAO;
	
	@Autowired
    private AppointmentDAO appointmentDAO;
	
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final String OBJECT_ANALYSIS_STARTED = "Analyzing data object ";
    
    private static final String OBJECT_EXTRACTION_STARTED = "Object extraction started ";
    private static final String OBJECT_EXTRACTION_COMPLETED = "Object extraction completed ";
    private static final String OBJECT_EXTRACTION_FAILED = "Object extraction failed ";
    private static final String OBJECT_EXTRACTION_SKIPPED = "Object extraction skipped ";
    
    private static final String EXTRACTION_PROCESS_STARTED = ">>>>>Extraction plugin started";
    private static final String EXTRACTION_PROCESS_COMPLETED = ">>>>>Extraction plugin completed ";
    
    private static final String COMPRESSION_STARTED = "Compression started";
    private static final String COMPRESSION_COMPLETED = "Compression completed ";

    /**
     * This class must be public and have a public default constructor for it to be usable by the osgi Service Component
     */
    public ExtractorAction() {
        this.logger.debug("initialized ExtractAction default constructor");
    }
    
    public void setPersonIdentityDAO(PersonIdentityDAO personIdentityDAO) {
		this.personIdentityDAO = personIdentityDAO;
	}
	
    public PersonIdentityDAO getPersonIdentityDAO() {
		return this.personIdentityDAO;
	}
	
	public void setAppointmentDAO(AppointmentDAO appointmentDAO) {
		this.appointmentDAO = appointmentDAO;
	}
	
	public AppointmentDAO getAppointmentDAO() {
		return this.appointmentDAO;
	}
	
    public void doDestroy() throws Exception {
    	System.out.println("ExtractorAction.doDestroy()");
  	}
    
    private Metainfo create(String id, String type, String destination) {
        Metainfo info = new Metainfo();
        info.setBackupDate(new Date());
        info.setDestination(destination);
        info.setId(id);
        info.setSource("extractor");
        info.setType(type);
        return info;
    }
    
    @Override
    public void doAction(Map<String, String> authData, Map<String, String> properties, List<String> options,
            Storage storage, BackupJobExecutionDTO job, Progressable progressor) throws ActionException {
        
    	int itemsExtracted = 0;
        int itemsSkipped = 0;
        
        this.logger.debug("Starting file analysis...");
        progressor.progress(EXTRACTION_PROCESS_STARTED);

        Date extrationTimestamp = new Date();
        try {
            Iterator<DataObject> dataObjects = storage.getDataObjects();
            while (dataObjects.hasNext()) {
                try {
                    DataObject dob = dataObjects.next();
                    progressor.progress(OBJECT_ANALYSIS_STARTED + dob.getPath());

                    String mime = null;
                    Map<String, String> meta = new HashMap<>();
                    String fulltext = null;

                    if (!extract(dob)) {
                        progressor.progress(OBJECT_EXTRACTION_SKIPPED + dob.getPath());
                        itemsSkipped++;
                    }

                    progressor.progress(OBJECT_EXTRACTION_STARTED + dob.getPath());
                    
                } catch (Exception e) {
                    itemsSkipped++;
                    progressor.progress(OBJECT_EXTRACTION_FAILED + " " + e.toString());
                }
            }
            
            // compress db folder and add to storage
            String sourceFolderName =  "entities";
            String outputFileName = "entities.zip";
            compress(sourceFolderName, outputFileName, progressor);
            
            progressor.progress("Creating metadata for entity archive.");
            
            MetainfoContainer metadata = new MetainfoContainer();
            String type = new MimetypesFileTypeMap().getContentType(outputFileName);
            metadata.addMetainfo(create("1", type, outputFileName));
            
            progressor.progress("Adding entity archive to storage.");
            storage.addFile(new FileInputStream(outputFileName), outputFileName, metadata);
            
        } catch (Exception e) {
            throw new ActionException(e);
        }

        progressor.progress(EXTRACTION_PROCESS_COMPLETED + " \n\t# of items extracted OK: " + itemsExtracted + " , SKIPPED: " + itemsSkipped );
        
    }

    private boolean extract(DataObject dob) {
    	// validate and recognise backup types before extraction
    	
    	//TODO: do additional checks
        if (dob.getPath().endsWith(".vcf"))
            return extractVisitCards(dob);
        if (dob.getPath().endsWith(".ics"))
            return extractCalendarEvents(dob);

        return false;
    }
    
    private boolean extractVisitCards(DataObject dob) {
    	try {
    		ByteArrayInputStream bis = new ByteArrayInputStream(dob.getBytes());
    		ParserChainTextReader textReader = Ezvcard.parse(bis);
			List<VCard> vcards = textReader.all();
			for(VCard vcard : vcards) {
				PersonIdentity ident = new PersonIdentity();
				if (vcard.getStructuredName() != null) {
					String tmp;
					ident.setFamilyName( ((tmp = vcard.getStructuredName().getFamily()) == null) ? "" : tmp);
					ident.setSurName( ((tmp = vcard.getStructuredName().getGiven()) == null) ? "" : tmp);
					tmp = "";
					for( String prefix :  vcard.getStructuredName().getPrefixes())
						tmp += prefix;
					ident.setPrefix( tmp );
					
					System.out.println(ident.getFamilyName());
					System.out.println(ident.getSurName());
					System.out.println(ident.getPrefix());
				}
				
//				for(Nickname nickname : vcard.getNicknames()) 
//					System.out.println(nickname.getValues().toString());
//				for(Title title : vcard.getTitles()) 
//					System.out.println(title.getValue());
//				for(Organization organisation : vcard.getOrganizations())
//					System.out.println(organisation.getValues().toString());
//				for(Address address : vcard.getAddresses()) {
//					System.out.println(address.getStreetAddress());
//					System.out.println(address.getLocality());
//					System.out.println(address.getRegion());
//					System.out.println(address.getPostalCode());
//					System.out.println(address.getCountry());
//					System.out.println(address.getLabel());
//					System.out.println(address.getTypes());
//				}
//				for(Telephone tel : vcard.getTelephoneNumbers())
//					System.out.println(tel.getTypes());
//				for(Email email : vcard.getEmails()) {
//					System.out.println(email.getValue());
//					System.out.println(email.getTypes());
//				}
//				for(Photo photo : vcard.getPhotos())
//					System.out.println(photo.getData());
//				for(FormattedName name : vcard.getFormattedNames() )
//					System.out.println(name.getValue());
				personIdentityDAO.saveOrUpdate(ident);
			}
			
			
		} catch (IOException e) {
			System.out.println(e.toString());
		} finally {
		}

        return true;
    }
    
    private boolean extractCalendarEvents(DataObject dob) {
    	
    	try {
	    	ByteArrayInputStream bis = new ByteArrayInputStream(dob.getBytes());
	
	    	CalendarBuilder builder = new CalendarBuilder();
	
	    	Calendar calendar = builder.build(bis);
	    	
	    	ComponentList comps = calendar.getComponents();
	    	
	    	for (Iterator i = comps.iterator(); i.hasNext();) {
	    		  Appointment event = new Appointment();
	    	      Component component = (Component) i.next();
	    	      System.out.println("Component [" + component.getName() + "]");

	    	      for (Iterator j = component.getProperties().iterator(); j.hasNext();) {
	    	          Property property = (Property) j.next();
	    	          System.out.println("Property [" + property.getName() + ", " + property.getValue() + "]");
	    	          if(property.getName().equals("DESCRIPTION"))
	    	        	  event.setDescription(property.getValue());
	    	          if(property.getName().equals("CREATED"))
	    	        	  event.setDateCreated(property.getValue());
	    	          if(property.getName().equals("DTSTAMP"))
	    	        	  event.setDateStamp(property.getValue());
	    	          if(property.getName().equals("DTSTART"))
	    	        	  event.setDateStart(property.getValue());
	    	          if(property.getName().equals("DTEND"))
	    	        	  event.setDateEnd(property.getValue()); 
	    	          if(property.getName().equals("STATUS"))
	    	        	  event.setStatus(property.getValue());
	    	          if(property.getName().equals("SUMMARY"))
	    	        	  event.setSummary(property.getValue());
	    	      }
	    	      appointmentDAO.saveOrUpdate(event);
	    	  }
    	} catch (IOException e) {
			System.out.println(e.toString());
		} catch (ParserException e) {
			System.out.println(e.toString());
		} finally {
		}

    	return true;
    }
    
    public void compress(String sourceFolderName, String outputFileName, Progressable progressor) throws IOException {
	    FileOutputStream fos = new FileOutputStream(outputFileName);
	    ZipOutputStream zos = new ZipOutputStream(fos);
	    //level - the compression level (0-9)
	    zos.setLevel(9);
	    progressor.progress("Begin to compress folder : " + sourceFolderName + " to " + outputFileName);
	    //File base = new File(sourceFolderName);
	    //String baseFolderName = base.getParent().get;
	    compressFolder(zos, sourceFolderName, /*sourceFolderName,*/ progressor);
	
	    zos.close();
	    progressor.progress("Compression ended successfully.");
    }
	    
    
    private void compressFolder(ZipOutputStream zos, String folderName, /*String baseFolderName,*/ Progressable progressor) throws IOException {
        File f = new File(folderName);
        if(f.exists()){
 
            if(f.isDirectory()){
                /*if(!folderName.equalsIgnoreCase(baseFolderName)){
                    String entryName = folderName.substring(baseFolderName.length()+1, folderName.length()) + File.separatorChar;
                    progressor.progress("Adding folder entry " + entryName);
                    ZipEntry ze= new ZipEntry(entryName);
                    zos.putNextEntry(ze);    
                }*/
                File f2[] = f.listFiles();
                for(int i=0; i<f2.length; i++){
                	//compressFolder(zos, f2[i].getAbsolutePath(), baseFolderName, progressor);
                	compressFolder(zos, f2[i].getPath(), /*baseFolderName,*/ progressor);
                }
            }else{
                String entryName = folderName /*.substring(baseFolderName.length()+1, folderName.length())*/;
                progressor.progress("Adding file entry " + entryName + "...");
                ZipEntry ze = new ZipEntry(entryName);
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream(folderName);
                int len;
                byte buffer[] = new byte[1024];
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
                zos.closeEntry();
            }
        }else{
        	progressor.progress("File or directory not found " + folderName);
        }
    }
}
