package br.ufc.quixada.tcc.osm.model;

import java.util.ArrayList;

import com.graphhopper.reader.OSMRelation.Member;

public class RelationOSM extends GenericOsmElement {
	protected final ArrayList<Member> members = new ArrayList<Member>(5);

	public RelationOSM(long id) {
		super(id, RELATION);
	}

	public ArrayList<Member> getMembers() {
		return members;
	}

	public boolean isMetaRelation() {
		for (Member member : members) {
			if (member.type() == RELATION) {
				return true;
			}
		}
		return false;
	}

	public boolean isMixedRelation() {
		boolean hasRel = false;
		boolean hasOther = false;

		for (Member member : members) {
			if (member.type() == RELATION) {
				hasRel = true;
			} else {
				hasOther = true;
			}

			if (hasRel && hasOther) {
				return true;
			}
		}
		return false;
	}

	public void removeRelations() {
		for (int i = members.size() - 1; i >= 0; i--) {
			if (members.get(i).type() == RELATION) {
				members.remove(i);
			}
		}
	}

	public void add(Member member) {
		members.add(member);
	}

	public static class Member {
		public static final int NODE = 0;
		public static final int WAY = 1;
		public static final int RELATION = 2;
		private final int type;
		private final long ref;
		private final String role;

		public Member(Member input) {
			type = input.type;
			ref = input.ref;
			role = input.role;
		}

		public Member(int type, long ref, String role) {
			this.type = type;
			this.ref = ref;
			this.role = role;
		}

		public String toString() {
			return "Member " + type + ":" + ref;
		}

		public int type() {
			return type;
		}

		public String role() {
			return role;
		}

		public long ref() {
			return ref;
		}
	}
}
