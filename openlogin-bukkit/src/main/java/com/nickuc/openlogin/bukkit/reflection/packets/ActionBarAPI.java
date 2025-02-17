/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import com.nickuc.openlogin.bukkit.reflection.ServerVersion;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import static com.nickuc.openlogin.bukkit.reflection.ReflectionUtils.getNMS;

public class ActionBarAPI extends Packet {

    private static boolean available = true;
    private static Method a;
    private static Object typeMessage;
    private static Constructor<?> chatConstructor;

    public static void sendActionBar(Player player, String message) {
        if (!available || !player.isOnline()) return;
        try {
            Object chatMessage = a.invoke(null, "{\"text\":\"" + message + "\"}");
            ServerVersion serverVersion = ServerVersion.getServerVersion();
            Object packet;
            switch (serverVersion) {
                case v1_16:
                case v1_17:
                    packet = chatConstructor.newInstance(chatMessage, typeMessage, UUID.randomUUID());
                    break;

                default:
                    packet = chatConstructor.newInstance(chatMessage, typeMessage);
                    break;
            }
            sendPacket(player, packet);
        } catch (Exception e) {
            available = false;
            e.printStackTrace();
        }
    }

    static {
        try {
            Class<?> icbc = getNMS("IChatBaseComponent");
            Class<?> ppoc = getNMS("PacketPlayOutChat");

            if (icbc.getDeclaredClasses().length > 0) {
                a = icbc.getDeclaredClasses()[0].getMethod("a", String.class);
            } else {
                a = getNMS("ChatSerializer").getMethod("a", String.class);
            }

            Class<?> typeMessageClass;
            ServerVersion serverVersion = ServerVersion.getServerVersion();
            boolean newConstructor = false;
            switch (serverVersion) {
                case v1_16:
                case v1_17:
                    newConstructor = true;

                case v1_12:
                case v1_13:
                case v1_14:
                case v1_15:
                    typeMessageClass = getNMS("ChatMessageType");
                    typeMessage = typeMessageClass.getEnumConstants()[2];
                    break;

                default:
                    typeMessageClass = byte.class;
                    typeMessage = (byte) 2;
                    break;
            }
            chatConstructor = newConstructor ? ppoc.getConstructor(icbc, typeMessageClass, UUID.class) : ppoc.getConstructor(icbc, typeMessageClass);
        } catch (Throwable e) {
            available = false;
            e.printStackTrace();
        }
    }

}