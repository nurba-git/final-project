package com.narxoz.darkrealm.systems;

import java.util.ArrayList;
import java.util.List;

/**
 * SOLID-S: только управление квестами.
 * GameScreen вызывает notifyKill(type) и notifyCollect(type).
 */
public class QuestManager {

    public enum QuestState { INACTIVE, ACTIVE, DONE }

    public static class Quest {
        public final String id, name, description, reward;
        public final int zone;
        public int progress, goal;
        public QuestState state = QuestState.INACTIVE;
        public int expReward;

        public Quest(String id, String name, String desc, String reward,
                     int zone, int goal, int exp) {
            this.id = id; this.name = name; this.description = desc;
            this.reward = reward; this.zone = zone; this.goal = goal;
            this.expReward = exp;
        }

        public boolean isDone() { return state == QuestState.DONE; }
        public float getFraction() { return goal > 0 ? (float) progress / goal : 0; }
    }

    private final List<Quest> quests = new ArrayList<>();
    private final List<Quest> justCompleted = new ArrayList<>();

    public QuestManager() {
        // Zone 1
        quests.add(new Quest("first_shadow",  "The First Shadow",
            "Defeat 5 Shadow Wraiths", "300 EXP + Iron Sword",   1, 5,  300));
        quests.add(new Quest("lost_dark",     "Lost in the Dark",
            "Find the missing scout",  "200 EXP + Healing Potion x3", 1, 1, 200));
        // Zone 2
        quests.add(new Quest("shattered",     "Shattered Ramparts",
            "Destroy 3 Cursed Barricades", "500 EXP + Steel Armor", 2, 3, 500));
        quests.add(new Quest("antidote",      "Antidote Run",
            "Collect 5 Moonbloom Herbs",   "400 EXP + Antidote x5", 2, 5, 400));
        // Zone 3
        quests.add(new Quest("void_crystals", "Voices in the Void",
            "Destroy 4 Void Crystals",     "800 EXP + Mana Orb",   3, 4, 800));
        quests.add(new Quest("silence",       "Silence Malgrath",
            "Defeat the final boss",        "Game Complete",         3, 1, 0));
    }

    public void activateZoneQuests(int zone) {
        for (Quest q : quests) {
            if (q.zone == zone && q.state == QuestState.INACTIVE)
                q.state = QuestState.ACTIVE;
        }
    }

    /** Уведомить об убийстве врага типа type. */
    public void notifyKill(String type) {
        if (type.equals("shadow_wraith")) notify("first_shadow", 1);
        if (type.equals("malgrath"))      notify("silence", 1);
    }

    /** Уведомить об уничтожении объекта. */
    public void notifyDestroy(String type) {
        if (type.equals("barricade"))    notify("shattered", 1);
        if (type.equals("void_crystal")) notify("void_crystals", 1);
    }

    /** Уведомить о сборе предмета. */
    public void notifyCollect(String type) {
        if (type.equals("herb"))  notify("antidote", 1);
        if (type.equals("scout")) notify("lost_dark", 1);
    }

    private void notify(String id, int amount) {
        for (Quest q : quests) {
            if (q.id.equals(id) && q.state == QuestState.ACTIVE) {
                q.progress = Math.min(q.goal, q.progress + amount);
                if (q.progress >= q.goal) {
                    q.state = QuestState.DONE;
                    justCompleted.add(q);
                }
            }
        }
    }

    public List<Quest> getActive()   {
        List<Quest> r = new ArrayList<>();
        for (Quest q : quests) if (q.state == QuestState.ACTIVE) r.add(q);
        return r;
    }

    public List<Quest> getJustCompleted() {
        List<Quest> r = new ArrayList<>(justCompleted);
        justCompleted.clear();
        return r;
    }

    public int countDone() {
        int n = 0; for (Quest q : quests) if (q.isDone()) n++; return n;
    }

    public List<Quest> getAll() { return quests; }
}
