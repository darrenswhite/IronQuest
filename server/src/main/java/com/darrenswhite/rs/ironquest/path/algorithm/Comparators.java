package com.darrenswhite.rs.ironquest.path.algorithm;

import com.darrenswhite.rs.ironquest.player.Player;
import com.darrenswhite.rs.ironquest.player.QuestPriority;
import com.darrenswhite.rs.ironquest.quest.Quest;
import com.darrenswhite.rs.ironquest.quest.requirement.SkillRequirement;
import com.darrenswhite.rs.ironquest.quest.reward.QuestRewards;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Convenient class for common {@link Comparator<Quest>} variants.
 */
public class Comparators {

  private Comparators() {
  }

  /**
   * Returns a {@link Comparator<Quest>} that compares by {@link QuestPriority}.
   *
   * @return a comparator that compares by priority
   * @see Player#getQuestPriority(Quest)
   */
  static Comparator<Quest> priority(Player player) {
    return Comparator.comparing(player::getQuestPriority);
  }

  /**
   * Returns a {@link Comparator<Quest>} that compares by:
   *
   * <ul>
   *   <li>No remaining skill requirements</li>
   *   <li>Maximum or high priority</li>
   * </ul>
   *
   * @return a comparator that compares by no requirements and maximum/high priority
   * @see Player#getRemainingSkillRequirements(Quest, boolean)
   * @see QuestPriority#greaterThanNormal()
   */
  static Comparator<Quest> noSkillRequirementsAndGreaterThanNormalPriority(Player player) {
    return Comparator.comparing((Quest quest) -> {
      Set<SkillRequirement> remainingSkillRequirements = player
          .getRemainingSkillRequirements(quest, true);
      QuestPriority priority = player.getQuestPriority(quest);

      return remainingSkillRequirements.isEmpty() && priority.greaterThanNormal();
    });
  }

  /**
   * Returns a {@link Comparator<Quest>} that compares by lowest remaining {@link
   * SkillRequirement}s.
   *
   * @param player the player
   * @return a comparator that compares by remaining skill requirements
   * @see Player#getTotalRemainingSkillRequirements(Quest, boolean)
   */
  static Comparator<Quest> remainingSkillRequirements(Player player) {
    return Comparator
        .comparing((Quest quest) -> player.getTotalRemainingSkillRequirements(quest, true))
        .reversed();
  }

  /**
   * Returns a {@link Comparator<Quest>} that compares by highest total {@link QuestRewards}.
   *
   * @param player the player
   * @return a comparator that compares by total rewards
   * @see Player#getTotalQuestRewards(Quest)
   */
  static Comparator<Quest> rewards(Player player) {
    return Comparator.comparing(player::getTotalQuestRewards);
  }

  /**
   * Returns a {@link Comparator<Quest>} that compares by highest score.
   *
   * @param player the player
   * @param scoringFunction the function used to calculate the score for a quest
   */
  static Comparator<Quest> scoring(Player player,
      BiFunction<Player, Quest, Double> scoringFunction) {
    return Comparator.comparingDouble(quest -> scoringFunction.apply(player, quest));
  }
}
