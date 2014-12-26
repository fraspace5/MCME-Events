/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mcmiddleearth.mcme.summerevent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Donovan
 */
class Team {
    public List<String> members = new ArrayList<>();
    public String name;
    public int score;
    public List<CapturePoint> cps = new ArrayList<>();
    public Team(String name){
        this.name = name;
    }
    public void addMembers(List<String> newbs){
        members.addAll(newbs);
    }
}
