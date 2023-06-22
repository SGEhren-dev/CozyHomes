package com.ehrengrenlund.cozyhomes.data;

import static com.ehrengrenlund.cozyhomes.utils.CozyUtils.FormatText;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class Home {
    private double x, y, z;
    private float pitch, yaw;
    private String name;
    private Identifier dim;

    public Home(double x, double y, double z, float pitch, float yaw, Identifier dim, String name) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.pitch = pitch;
        this.yaw = yaw;

        this.name = name;
        this.dim = dim;
    }

    public Home(Vec3d pos, float pitch, float yaw, Identifier dim, String name) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;

        this.pitch = pitch;
        this.yaw = yaw;

        this.name = name;
        this.dim = dim;
    }

    public static Home readFromNbt(NbtCompound nbt) {
        return new Home(
            nbt.getDouble("x"),
            nbt.getDouble("y"),
            nbt.getDouble("z"),
            nbt.getFloat("pitch"),
            nbt.getFloat("yaw"),
            Identifier.tryParse(nbt.getString("dim")),
            nbt.getString("name")
        );
    }

    public void writeToNbt(NbtCompound nbt) {
        nbt.putDouble("x", this.x);
        nbt.putDouble("y", this.y);
        nbt.putDouble("z", this.z);
        nbt.putFloat("pitch", this.pitch);
        nbt.putFloat("yaw", this.yaw);
        nbt.putString("dim", this.dim.toString());
        nbt.putString("name", this.name);
    }

    public double GetX() {
        return this.x;
    }

    public double GetY() {
        return this.y;
    }

    public double GetZ() {
        return this.z;
    }

    public float GetPitch() {
        return this.pitch;
    }

    public float GetYaw() {
        return this.yaw;
    }

    public String GetName() {
        return this.name;
    }

    public Identifier GetDimId() {
        return this.dim;
    }

    public MutableText ToText() {
        return Text.translatable("%s\n%s; %s; %s\n%s; %s\n%s",
            FormatText("Name", this.name),
            FormatText("X", this.x),
            FormatText("Y", this.y),
            FormatText("Z", this.z),
            FormatText("Yaw", this.yaw),
            FormatText("Pitch", this.pitch),
            FormatText("In", this.dim.toString())
        );
    }
}
