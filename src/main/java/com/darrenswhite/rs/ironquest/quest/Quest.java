package com.darrenswhite.rs.ironquest.quest;

import com.darrenswhite.rs.ironquest.IronQuest;
import com.darrenswhite.rs.ironquest.player.Player;
import com.darrenswhite.rs.ironquest.player.Skill;
import com.darrenswhite.rs.ironquest.quest.requirement.QuestRequirement;
import com.darrenswhite.rs.ironquest.quest.requirement.Requirement;
import com.darrenswhite.rs.ironquest.quest.requirement.SkillRequirement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
   * If the quest is members or free
   */
  private final boolean members;

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
   * Multiple sets of previously used skills on lamps
   */
  private Set<Set<Skill>> previousLampSkills = new HashSet<>();

  /**
   * Quest completion priority
   */
  private UserPriority userPriority;

  /**
   * Creates a new Quest instance
   *
   * @param id The Quest unique id
   * @param title The Quest title
   * @param displayName The Quest display name (title is used if null)
   * @param members If the quest is free or members
   * @param requirements The Quest Requirements
   * @param questPoints Skill XP rewards
   * @param skillRewards Lamp rewards
   * @param lampRewards Quest points reward
   */
  public Quest(int id, String title, String displayName, boolean members,
      Set<Requirement> requirements, int questPoints, Map<Skill, Integer> skillRewards,
      Set<Lamp> lampRewards) {
    this.id = id;
    this.title = Objects.requireNonNull(title);
    this.displayName = displayName != null ? displayName : title;
    this.members = members;
    this.requirements = Objects.requireNonNull(requirements);
    this.questPoints = questPoints;
    this.skillRewards = Objects.requireNonNull(skillRewards);
    this.lampRewards = Objects.requireNonNull(lampRewards);
    this.userPriority = UserPriority.NORMAL;
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

  public Set<Requirement> getOtherRequirements() {
    return requirements.stream().filter(Requirement::isOther).collect(Collectors.toSet());
  }

  /**
   * Get previously used skills in lamps. Used for exclusive Lamps
   *
   * @return Multiple sets of previously used skills
   */
  public Set<Set<Skill>> getPreviousLampSkills() {
    return previousLampSkills;
  }

  /**
   * Set previously used skills in lamps. Used for exclusive Lamps
   *
   * @param previousLampSkills Multiple sets of previously used skills
   */
  public void setPreviousLampSkills(Set<Set<Skill>> previousLampSkills) {
    this.previousLampSkills = previousLampSkills;
  }

  /**
   * Calculates the priority for this Quest based on skill requirements and rewards
   *
   * @param p The Player instance
   * @param ironman Test ironman mode
   * @param recommended Test recommended mode
   * @return The priority of this Quest
   */
  public int getPriority(Player p, boolean ironman, boolean recommended) {

    if (userPriority == UserPriority.LOW) {
      return Integer.MIN_VALUE;
    }

    if (userPriority == UserPriority.HIGH) {
      return Integer.MAX_VALUE;
    }

    // Get the total remaining skill requirements
    int reqs = getRemainingSkillRequirements(p, ironman, recommended).stream()
        .mapToInt(SkillRequirement::getLevel).sum();
    // Get the total rewards and scale down by a factor of 100
    int rwds = (getTotalLampRewards(p) + getTotalSkillRewards()) / 100;
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
    return requirements.stream().filter(r -> r instanceof QuestRequirement)
        .map(r -> (QuestRequirement) r).collect(Collectors.toSet());
  }

  /**
   * Gets the remaining Quests to complete for this quest
   *
   * @param player The player to check for
   * @return The remaining Quests
   */
  public Set<Quest> getRemainingQuestRequirements(Player player) {
    IronQuest ironQuest = IronQuest.getInstance();

    // Given the Quest IDs for the Quest Requirements,
    // filter out completed quests and then return a Set
    // of Quest objects mapped from the Quest ID
    return getQuestRequirements().stream()
        .filter(q -> !player.isQuestCompleted(q.getId()))
        .map(q -> ironQuest.getQuest(q.getId()))
        .collect(Collectors.toSet());
  }

  /**
   * Gets the remaining skill level requirements for this Quest
   *
   * @param p The Player instance
   * @param ironman Test ironman mode
   * @param recommended Test recommended mode
   * @return The remaining skill level requirements
   */
  public Set<SkillRequirement> getRemainingSkillRequirements(Player p, boolean ironman,
      boolean recommended) {
    // Use empty hashset first to squash duplicate requirements
    // This happens when there is a normal requirements and another
    // requirement for recommended or ironman
    // Using amalgamateRequirements will use the highest requirement
    Set<SkillRequirement> remaining = new HashSet<>();

    // Create a new Stream for the Skill requirements
    // Filter by removing requirements already met
    // Collect the results in a Map
    IronQuest.amalgamateRequirements(remaining,
        getSkillRequirements().stream().filter(r -> !r.test(p, ironman, recommended))
            .collect(Collectors.toSet()));

    // Create a new Stream for the Quest requirements
    // Filter by removing Quest's already completed
    // Map Quest ids to Quest objects
    // Add all Quest skill requirements
    getQuestRequirements().stream().filter(r -> !r.test(p, ironman, recommended))
        .map(r -> IronQuest.getInstance().getQuest(r.getId())).forEach(q -> {
      // Get remaining skill requirements
      Set<SkillRequirement> qRemaining = q.getRemainingSkillRequirements(p, ironman, recommended);

      IronQuest.amalgamateRequirements(remaining, qRemaining);
    });

    return remaining;
  }

  /**
   * Gets the Skill level requirements
   *
   * @return The Skill level requirements
   */
  public Set<SkillRequirement> getSkillRequirements() {
    return requirements.stream().filter(r -> r instanceof SkillRequirement)
        .map(r -> (SkillRequirement) r).collect(Collectors.toSet());
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
   * Gets the total Lamp rewards that the Player has the requirements for
   *
   * @param p The Player instance
   * @return The total Lamp reward values
   */
  private int getTotalLampRewards(Player p) {
    return lampRewards.stream().filter(l -> l.hasRequirements(p)).mapToInt(Lamp::getValue).sum();
  }

  /**
   * Gets the total Skill XP rewards
   *
   * @return The total Skill XP rewards
   */
  private int getTotalSkillRewards() {
    return skillRewards.values().stream().mapToInt(Integer::intValue).sum();
  }

  /**
   * Gets the quest's completion priority
   *
   * @return The quest's completion priority
   */
  public UserPriority getUserPriority() {
    return userPriority;
  }

  /**
   * Sets the quest's completion priority
   *
   * @param userPriority The priority level
   */
  public void setUserPriority(UserPriority userPriority) {
    this.userPriority = userPriority;
  }

  /**
   * Checks if the Player meets all 'other' requirements
   *
   * @param p The Player instance
   * @param ironman Test ironman mode
   * @param recommended Test recommended mode
   * @return If the Player meets all other requirements
   */
  public boolean hasOtherRequirements(Player p, boolean ironman, boolean recommended) {
    return getOtherRequirements().stream().allMatch(r -> r.test(p, ironman, recommended));
  }

  /**
   * Checks if the Player meets all Quest requirements
   *
   * @param p The Player instance
   * @param ironman Test ironman mode
   * @param recommended Test recommended mode
   * @return If the Player meets all Quest requirements
   */
  public boolean hasQuestRequirements(Player p, boolean ironman, boolean recommended) {
    return getQuestRequirements().stream().allMatch(r -> r.test(p, ironman, recommended));
  }

  /**
   * Checks if the Player meets all requirements
   *
   * @param p The Player instance
   * @param ironman Test ironman mode
   * @param recommended Test recommended mode
   * @return If the Player meets all requirements
   */
  public boolean hasRequirements(Player p, boolean ironman, boolean recommended) {
    return requirements.stream().allMatch(r -> r.test(p, ironman, recommended));
  }

  /**
   * Checks if the Player meets all Skill requirements
   *
   * @param p The Player instance
   * @param ironman Test ironman mode
   * @param recommended Test recommended mode
   * @return If the Player meets all Skill requirements
   */
  public boolean hasSkillRequirements(Player p, boolean ironman, boolean recommended) {
    return getSkillRequirements().stream().allMatch(r -> r.test(p, ironman, recommended));
  }

  /**
   * Test if this quest is free or members
   *
   * @return true if the quest is members
   */
  public boolean isMembers() {
    return members;
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

  public enum UserPriority {
    MAX,
    HIGH,
    NORMAL,
    LOW
  }
}
