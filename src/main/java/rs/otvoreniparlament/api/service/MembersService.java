package rs.otvoreniparlament.api.service;

import java.util.List;

import rs.otvoreniparlament.api.domain.Member;

public interface MembersService {

	List<Member> getMembers(int page, int limit, String sort);
	
	Member getMember(int id);

}
