package com.darrenswhite.rs.ironquest.quest.requirement;

import com.darrenswhite.rs.ironquest.player.Player;
import com.darrenswhite.rs.ironquest.player.Skill;

/**
 * @author Darren White
 */
public class SkillRequirement extends Requirement {

	private final Skill skill;
	private final int level;

	public SkillRequirement(Skill skill, int level) {
		this(false, false, skill, level);
	}

	public SkillRequirement(boolean ironman, boolean recommended,
	                        Skill skill, int level) {
		super(ironman, recommended);
		this.skill = skill;
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public Skill getSkill() {
		return skill;
	}

	@Override
	public boolean test(Player p) {
		return p.getLevel(skill) >= level;
	}
}