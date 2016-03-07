package com.thebubblenetwork.bubblebungee;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * Copyright Statement
 * ----------------------
 * Copyright (C) The Bubble Network, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Wrote by Jacob Evans <jacobevansminor@gmail.com>, 01 2016
 * <p/>
 * <p/>
 * Class information
 * ---------------------
 * Package: com.thebubblenetwork.bubblebungee
 * Date-created: 28/01/2016 08:24
 * Project: BubbleBungee
 */
public class P extends Plugin {
    private BubbleBungee bubbleBungee;

    public P() {
        super();
        bubbleBungee = new BubbleBungee(this);
    }

    public void onLoad() {
        bubbleBungee.onLoad();
    }

    public void onEnable() {
        bubbleBungee.onEnable();
    }

    public void onDisable() {
        bubbleBungee.onDisable();
    }
}
