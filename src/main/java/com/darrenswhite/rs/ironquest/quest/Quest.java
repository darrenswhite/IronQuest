package com.darrenswhite.rs.ironquest.quest;

import com.darrenswhite.rs.ironquest.IronQuest;
import com.darrenswhite.rs.ironquest.player.Player;
import com.darrenswhite.rs.ironquest.player.Skill;
import com.darrenswhite.rs.ironquest.quest.requirement.QuestRequirement;
import com.darrenswhite.rs.ironquest.quest.requirement.Requirement;
import com.darrenswhite.rs.ironquest.quest.requirement.SkillRequirement;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Darren White
 */
public class Quest {

	/**
	 * The Quest unique id
	 */
	private final int id;

	/**
	 * The Quest title
	 */
	private final String title;

	/**
	 * The name to display for the Quest
	 */
	private final String displayName;

	/**
	 * The quest Requirements
	 */
	private final Set<Requirement> requirements;

	/**
	 * Skill XP rewards
	 */
	private final Map<Skill, Integer> skillRewards;

	/**
	 * Lamp rewards
	 */
	private final Set<Lamp> lampRewards;

	/**
	 * Quest points reward
	 */
	private final int questPoints;

	/**
	 * Creates a new Quest instance
	 *
	 * @param id           The Quest unique id
	 * @param title        The Quest title
	 * @param displayName  The Quest display name (title is used if null)
	 * @param requirements The Quest Requirements
	 * @param questPoints  Skill XP rewards
	 * @param skillRewards Lamp rewards
	 * @param lampRewards  Quest points reward
	 */
	public Quest(int id, String title, String displayName,
	             Set<Requirement> requirements, int questPoints,
	             Map<Skill, Integer> skillRewards, Set<Lamp> lampRewards) {
		this.id = id;
		this.title = Objects.requireNonNull(title);
		this.displayName = displayName != null ? displayName : title;
		this.requirements = Objects.requireNonNull(requirements);
		this.questPoints = questPoints;
		this.skillRewards = Objects.requireNonNull(skillRewards);
		this.lampRewards = Objects.requireNonNull(lampRewards);
	}

	/**
	 * Gets this Quest display name
	 *
	 * @return The Quest display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Gets this Quest unique id
	 *
	 * @return The Quest id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets this Quest lamp rewards
	 *
	 * @return A set of Lamp rewards
	 */
	public Set<Lamp> getLampRewards() {
		return Collections.unmodifiableSet(lampRewards);
	}

	/**
	 * Calculates the priority for this Quest based on skill requirements
	 * and rewards
	 *
	 * @param p The Player instance
	 * @return The priority of this Quest
	 */
	public int getPriority(Player p) {
		// Get the total remaining skill requirements
		int reqs = getRemainingSkillRequirements(p).stream()
				.mapToInt(SkillRequirement::getLevel)
				.sum();
		// Get the total rewards and scale down by a factor of 100
		int rwds = (getTotalLampRewards() + getTotalSkillRewards()) / 100;

		// Priority is low requirements and high rewards
		return rwds - reqs;
	}

	/**
	 * Gets the amount of quest points for this Quest
	 *
	 * @return The number of Quest points
	 */
	public int getQuestPoints() {
		return questPoints;
	}

	/**
	 * Gets the Quest requirements
	 *
	 * @return The Quest requirements
	 */
	public Set<QuestRequirement> getQuestRequirements() {
		return requirements.stream()
				.filter(r -> r instanceof QuestRequirement)
				.map(r -> (QuestRequirement) r)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the remaining skill level requirements for this Quest
	 *
	 * @param p The Player instance
	 * @return The remaining skill level requirements
	 */
	public Set<SkillRequirement> getRemainingSkillRequirements(Player p) {
		// Create a new Stream for the Skill requirements
		// Filter by removing requirements already met
		// Collect the results in a Map
		Set<SkillRequirement> remaining = getSkillRequirements().stream()
				.filter(r -> !r.test(p))
				.collect(Collectors.toSet());

		// Create a new Stream for the Quest requirements
		// Filter by removing Quest's already completed
		// Map Quest ids to Quest objects
		// Add all Quest skill requirements
		getQuestRequirements().stream()
				.filter(r -> !r.test(p))
				.map(r -> IronQuest.getInstance().getQuest(r.getId()))
				.forEach(q -> {
					// Get remaining skill requirements
					Set<SkillRequirement> qRemaining =
							q.getRemainingSkillRequirements(p);

					// Add them to the remaining map
					// if they are larger or not present
					qRemaining.forEach(qr -> {
						Optional<SkillRequirement> req = remaining.stream()
								.filter(r -> r.getSkill() == qr.getSkill())
								.findAny();

						if (req.isPresent()) {
							if (qr.getLevel() > req.get().getLevel()) {
								remaining.remove(req.get());
								remaining.add(qr);
							}
						} else {
							remaining.add(qr);
						}
					});
				});

		return remaining;
	}

	/**
	 * Gets the Skill level requirements
	 *
	 * @return The Skill level requirements
	 */
	public Set<SkillRequirement> getSkillRequirements() {
		return requirements.stream()
				.filter(r -> r instanceof SkillRequirement)
				.map(r -> (SkillRequirement) r)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the Skill XP rewards
	 *
	 * @return The Skill XP rewards
	 */
	public Map<Skill, Integer> getSkillRewards() {
		return Collections.unmodifiableMap(skillRewards);
	}

	/**
	 * Gets this Quest title
	 *
	 * @return The Quest title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the total Lamp rewards
	 *
	 * @return The total Lamp reward values
	 */
	private int getTotalLampRewards() {
		return lampRewards.stream()
				.mapToInt(Lamp::getValue)
				.sum();
	}

	/**
	 * Gets the total Skill XP rewards
	 *
	 * @return The total Skill XP rewards
	 */
	private int getTotalSkillRewards() {
		return skillRewards.values().stream()
				.mapToInt(Integer::intValue)
				.sum();
	}

	/**
	 * Checks if the Player meets all 'other' requirements
	 *
	 * @param p The Player instance
	 * @return If the Player meets all other requirements
	 */
	public boolean hasOtherRequirements(Player p) {
		return requirements.stream()
				.filter(r -> r.getClass().equals(Requirement.class))
				.filter(r -> !r.test(p))
				.count() == 0;
	}

	/**
	 * Checks if the Player meets all Quest requirements
	 *
	 * @param p The Player instance
	 * @return If the Player meets all Quest requirements
	 */
	public boolean hasQuestRequirements(Player p) {
		return getQuestRequirements().stream()
				.filter(r -> !r.test(p))
				.count() == 0;
	}

	/**
	 * Checks if the Player meets all requirements
	 *
	 * @param p The Player instance
	 * @return If the Player meets all requirements
	 */
	public boolean hasRequirements(Player p) {
		return requirements.stream().filter(r -> !r.test(p)).count() == 0;
	}

	/**
	 * Checks if the Player meets all Skill requirements
	 *
	 * @param p The Player instance
	 * @return If the Player meets all Skill requirements
	 */
	public boolean hasSkillRequirements(Player p) {
		return getSkillRequirements().stream()
				.filter(r -> !r.test(p))
				.count() == 0;
	}

	@Override
	public String toString() {
		return "Quest{" +
				"id=" + id +
				", title='" + title + '\'' +
				", displayName='" + displayName + '\'' +
				", requirements=" + requirements +
				", skillRewards=" + skillRewards +
				", lampRewards=" + lampRewards +
				'}';
	}
}