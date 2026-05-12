package com.narxoz.darkrealm.interfaces;

/** SOLID-I: только логика обновления. Статичные тайлы НЕ реализуют это. */
public interface IUpdatable {
    void update(float delta);
}
