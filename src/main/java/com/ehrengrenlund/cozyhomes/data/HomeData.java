package com.ehrengrenlund.cozyhomes.data;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.List;
import java.util.ArrayList;

public class HomeData implements ComponentV3 {
    private final List<Home> homes = new ArrayList<>();
    private int maxHomes;

    public void readFromNbt(NbtCompound nbt) {
        homes.clear();
        nbt.getList("homes", NbtType.COMPOUND).forEach(v -> homes.add(Home.readFromNbt((NbtCompound) v)));
        maxHomes = nbt.getInt("maxHomes");
    }

    public void writeToNbt(NbtCompound tag) {
        NbtList homeTag = new NbtList();

        homes.forEach(v -> {
            NbtCompound homeNbt = new NbtCompound();
            v.writeToNbt(homeNbt);
            homeTag.add(homeNbt);
        });

        tag.put("homes", homeTag);
        tag.putInt("maxHomes", maxHomes);
    }

    public List<Home> getHomes() {
        return this.homes;
    }

    public int getMaxHomes() {
        return this.maxHomes;
    }

    public boolean addHome(Home home) {
        if (homes.stream().anyMatch(nbt -> nbt.GetName().equalsIgnoreCase(home.GetName())))
            return false;

        return homes.add(home);
    }

    public boolean removeHome(String name) {
        if (homes.stream().noneMatch(nbt -> nbt.GetName().equalsIgnoreCase(name)))
            return false;

        return homes.removeIf(nbt -> nbt.GetName().equalsIgnoreCase(name));
    }
}
