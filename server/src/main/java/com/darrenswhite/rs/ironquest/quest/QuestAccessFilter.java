package com.darrenswhite.rs.ironquest.quest;

/**
 * Enum representing quest access filters.
 *
 * @author Darren S. White
 */
public enum QuestAccessFilter {

  ALL,
  FREE,
  MEMBERS;

  public boolean isFree() {
    return this == ALL || this == FREE;
  }

  public boolean isMembers() {
    return this == ALL || this == MEMBERS;
  }
}
