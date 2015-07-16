/**
 * @author richard
 * 
 * returns Parameter Objects with all fields
 */

package facebook.storage;

import com.restfb.Parameter;
import com.restfb.types.Album;
import com.restfb.types.Comment;
import com.restfb.types.Group;
import com.restfb.types.Page;
import com.restfb.types.Photo;
import com.restfb.types.Post;
import com.restfb.types.User;

public enum MasterParameter
{
	ALBUM(Parameter.with("field", "id,can_upload,count,cover_photo,created_time,description,event,from,link,location,name,place,privacy,type,updated_time"),
			Album.class),
	COMMENT(Parameter.with("fields", "id,attachment,can_comment,can_remove,can_hide,can_like,comment_count,created_time,from,like_count,message,message_tags,object,parent,user_likes"),
			Comment.class),
	GROUP(Parameter.with("fields", "id,cover,description,email,icon,link,member_request_count,name,owner,parent,privacy,updated_time"),
			Group.class),
	PAGE(Parameter.with("fields", "id,about,access_token,affiliation,app_id,artists_we_like,attire,awards,band_interests,band_members,best_page,bio,birthday,booking_agent,built,business,can_post,category,category_list,company_overview,cover,culinary_team,current_location,description,description_html,directed_by,emails,features,food_styles,founded,general_info,general_manager,genre,global_brand_page_name,has_added_app,hometown,hours,influences,is_community_page,is_permanently_closed,is_published,is_unclaimed,is_verified,link,location,mission,mpg,name,network,new_like_count,offer_eligible,parent_page,parking,payment_options,personal_info,personal_interests,pharma_safety_info,phone,plot_outline,press_contact,price_range,produced_by,products,promotion_eligible,promotion_ineligible_reason,public_transit,record_label,release_date,restaurant_services,restaurant_specialties,schedule,screenplay_by,season,starring,store_number,studio,unread_message_count,unread_notif_count,unseen_message_count,username,website,were_here_count,written_by,checkins,likes,members"),
			Page.class),
	PHOTO(Parameter.with("fields", "id,insights,tags,likes,comments,album,created_time,from,height,icon,images,link,name,name_tags,page_story_id,updated_time,width,place,backdated_time,picture"),
			Photo.class),
	POST(Parameter.with("fields", "id,admin_creator,application,call_to_action,caption,created_time,description,feed_targeting,from,icon,is_hidden,is_published,link,message,message_tags,name,object_id,picture,place,privacy,properties,shares,source,status_type,story,story_tags,targeting,to,type,updated_time,with_tags"),
			Post.class),
	USER(Parameter.with("fields", "about,address,age_range,bio,birthday,context,currency,devices,education,email,first_name,gender,hometown,inspirational_people,install_type,installed,interested_in,is_verified,languages,last_name,link,location,meeting_for,middle_name,name,name_format,payment_pricepoints,test_group,political,relationship_status,religion,security_settings,significant_other,sports,quotes,third_party_id,timezone,updated_time,verified,video_upload_limits,viewer_can_send_gift,website,work,cover"),
			User.class),
	EMPTY(Parameter.with("null", ""), Object.class);

	private Parameter param;
	private Class<?> type;

	private MasterParameter(Parameter param, Class<?> type)
	{
		this.param = param;
		this.type = type;
	}

	public Parameter getParameter()
	{
		return param;
	}

	public Class<?> getParameterClass()
	{
		return type;
	}

	public static Parameter getParameterByClass(Class<?> typeClass)
	{
		return getByClass(typeClass).getParameter();
	}

	public static MasterParameter getByClass(Class<?> typeClass)
	{
		for (MasterParameter mp : MasterParameter.values())
			if (mp.getParameterClass().equals(typeClass))
				return mp;
		return MasterParameter.EMPTY;
	}
}
