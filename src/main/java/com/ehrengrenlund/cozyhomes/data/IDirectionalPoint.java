package com.ehrengrenlund.cozyhomes.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public interface IDirectionalPoint {
    double GetX();
    double GetY();
    double GetZ();
    float GetPitch();
    float GetYaw();
    String GetName();
    Vec3d GetCoords();
    Identifier GetDimId();
    MutableText ToText(MinecraftServer server);}
