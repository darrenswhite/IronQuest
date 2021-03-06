package com.darrenswhite.rs.ironquest.path.algorithm;

import com.darrenswhite.rs.ironquest.path.QuestNotFoundException;
import com.darrenswhite.rs.ironquest.player.QuestPriority;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

class DefaultAlgorithmTest extends PathFinderAlgorithmTest {

  static final DefaultAlgorithm ALGORITHM = new DefaultAlgorithm();

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class GetQuestComparator {

    @Test
    void shouldPrioritiseLowRequirementsAndHighRewards() throws QuestNotFoundException {
      assertQuestOrder(ALGORITHM, QUEST_NO_REQUIREMENTS_200K_DEFENCE_REWARD,
          QUEST_NO_REQUIREMENTS_50K_MAGIC_REWARD, QUEST_NO_REQUIREMENTS_45K_MAGIC_LAMP_REWARD,
          QUEST_49_MAGIC_REQUIREMENT_NO_REWARDS,
          QUEST_50_RUNECRAFTING_REQUIREMENT_59_RUNECRAFTING_IRONMAN_REQUIREMENT_NO_REWARDS);
    }

    @Test
    void shouldIncludeIronmanRequirements() throws QuestNotFoundException {
      assertQuestOrder(ALGORITHM, true, QUEST_NO_REQUIREMENTS_200K_DEFENCE_REWARD,
          QUEST_NO_REQUIREMENTS_50K_MAGIC_REWARD, QUEST_NO_REQUIREMENTS_45K_MAGIC_LAMP_REWARD,
          QUEST_49_MAGIC_REQUIREMENT_NO_REWARDS,
          QUEST_50_RUNECRAFTING_REQUIREMENT_59_RUNECRAFTING_IRONMAN_REQUIREMENT_NO_REWARDS);
    }

    @Test
    void shouldPrioritiseMaximumQuestWithHighRequirementsAndNoRewards()
        throws QuestNotFoundException {
      Map<String, QuestPriority> priorities = Map
          .of(QUEST_49_MAGIC_REQUIREMENT_NO_REWARDS, QuestPriority.MAXIMUM);

      assertQuestOrder(ALGORITHM, priorities, QUEST_49_MAGIC_REQUIREMENT_NO_REWARDS,
          QUEST_NO_REQUIREMENTS_200K_DEFENCE_REWARD, QUEST_NO_REQUIREMENTS_50K_MAGIC_REWARD,
          QUEST_NO_REQUIREMENTS_45K_MAGIC_LAMP_REWARD,
          QUEST_50_RUNECRAFTING_REQUIREMENT_59_RUNECRAFTING_IRONMAN_REQUIREMENT_NO_REWARDS);
    }

    @Test
    void shouldPrioritiseMinimumQuestWithNoRequirementsAndHighRewards()
        throws QuestNotFoundException {
      Map<String, QuestPriority> priorities = Map
          .of(QUEST_NO_REQUIREMENTS_200K_DEFENCE_REWARD, QuestPriority.MINIMUM);

      assertQuestOrder(ALGORITHM, priorities, QUEST_NO_REQUIREMENTS_50K_MAGIC_REWARD,
          QUEST_NO_REQUIREMENTS_45K_MAGIC_LAMP_REWARD, QUEST_49_MAGIC_REQUIREMENT_NO_REWARDS,
          QUEST_50_RUNECRAFTING_REQUIREMENT_59_RUNECRAFTING_IRONMAN_REQUIREMENT_NO_REWARDS,
          QUEST_NO_REQUIREMENTS_200K_DEFENCE_REWARD);
    }

    @Test
    void shouldPrioritiseQuestPriorityInDescendingOrder() throws QuestNotFoundException {
      Map<String, QuestPriority> priorities = Map
          .of(QUEST_NO_REQUIREMENTS_200K_DEFENCE_REWARD, QuestPriority.MINIMUM,
              QUEST_NO_REQUIREMENTS_50K_MAGIC_REWARD, QuestPriority.LOW,
              QUEST_NO_REQUIREMENTS_45K_MAGIC_LAMP_REWARD, QuestPriority.HIGH,
              QUEST_49_MAGIC_REQUIREMENT_NO_REWARDS, QuestPriority.MAXIMUM);

      assertQuestOrder(ALGORITHM, priorities, QUEST_49_MAGIC_REQUIREMENT_NO_REWARDS,
          QUEST_NO_REQUIREMENTS_45K_MAGIC_LAMP_REWARD,
          QUEST_50_RUNECRAFTING_REQUIREMENT_59_RUNECRAFTING_IRONMAN_REQUIREMENT_NO_REWARDS,
          QUEST_NO_REQUIREMENTS_50K_MAGIC_REWARD, QUEST_NO_REQUIREMENTS_200K_DEFENCE_REWARD);
    }
  }
}
