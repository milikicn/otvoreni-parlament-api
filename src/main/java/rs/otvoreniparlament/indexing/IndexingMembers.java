package rs.otvoreniparlament.indexing;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.Build;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.translog.BufferedChecksumStreamOutput;

import rs.otvoreniparlament.api.dao.MembersDao;
import rs.otvoreniparlament.api.domain.Member;
import rs.otvoreniparlament.api.domain.Party;
import rs.otvoreniparlament.api.index.ElasticClient;

public class IndexingMembers {
	
	private MembersDao md;
	private List<Member> membersForIndexing;
	public IndexingMembers(){
		md = new MembersDao();
		membersForIndexing = md.getMembers(1, 10000 , "ASC", "");
	}
	private static final Logger logger = LogManager.getLogger(IndexingMembers.class);

	public void indexMembers (){
		for (Member member : membersForIndexing) {
			try {
				XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
				
				builder
                	.field("id", member.getId()!= null ? member.getId() : "-1" )
                    .field("name", member.getName()!= null ? member.getName() : "no data" )
                    .field("surname", member.getLastName()!= null ? member.getLastName() : "no data" )
                    .field("gender", member.getGender()!= null ? member.getGender() : "no data" )
                    .field("mail", member.getEmail() != null ? member.getEmail() : "no data" )
                    .field("biography", member.getBiography()!= null ? member.getBiography(): "no data" );
				if (member.getDateOfBirth()!= null){
					builder.field("dateofbirth", member.getDateOfBirth());
				}
				if (member.getPlaceOfBirth() != null) {
					builder.startObject("placeofbirth")
						.field("birth-town", member.getPlaceOfBirth().getName()!= null ? member.getPlaceOfBirth().getName() : "no data" )
						.field("birth-region", member.getPlaceOfBirth().getRegion()!= null ? member.getPlaceOfBirth().getRegion() : "no data")
						.field("birth-country", member.getPlaceOfBirth().getCountry()!= null ? member.getPlaceOfBirth().getCountry() : "no data")
					.endObject();
				}
				
				if (member.getPlaceOfResidence() != null) {
					builder.startObject("placeofresidence")
						.field("residence-town", member.getPlaceOfResidence().getName()!= null ? member.getPlaceOfResidence().getName() : "no data")
						.field("residence-region", member.getPlaceOfResidence().getRegion()!= null ? member.getPlaceOfResidence().getRegion() : "no data")
						.field("residence-country", member.getPlaceOfResidence().getCountry()!= null ? member.getPlaceOfResidence().getCountry() : "no data")
					.endObject();
				}
				builder.startArray("member-parties");
				
				List<Party> parties = member.getParties();
				for (Party party : parties) {
					builder.startObject()
						.field("party-id", party.getId().toString()!= null ? party.getId() : "-1")
					.endObject();
				}
				
				builder.endArray();        
                
                builder.endObject();
				
				IndexResponse response = ElasticClient.getInstance().getClient().prepareIndex(IndexName.MEMBER_INDEX, IndexType.MEMBER_TYPE, member.getId().toString())
				        .setSource(builder)
				        .get();

//				Uncomment this to follow process of indexing
//				String _index = response.getIndex();
//				System.out.println(_index);
//				// Type name
//				String _type = response.getType();
//				
//				System.out.println(_type);
//				 Document ID (generated or not)
//				String _id = response.getId();
//				
//				System.out.println(_id);
//				// Version (if it's the first time you index this document, you will get: 1)
//				long _version = response.getVersion();
//				
//				System.out.println(_version);
//				// isCreated() is true if the document is a new one, false if it has been updated
//				boolean created = response.isCreated();
//				
//				System.out.println(created);
			} catch (IOException e) {
				logger.error(e);
				
			}
		}
	}
	
	public void deleteMembers(){
		ElasticClient.getInstance().getClient().admin().indices().delete(Requests.deleteIndexRequest(IndexName.MEMBER_INDEX));
	}
}
