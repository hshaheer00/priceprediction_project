
package com.example.prctc;
public class TabletRequest {
    public int battery_power, ram, storage, camera;
    public float screen_size;

    public TabletRequest(int battery_power, int ram, int storage, float screen_size, int camera) {
        this.battery_power = battery_power;
        this.ram = ram;
        this.storage = storage;
        this.screen_size = screen_size;
        this.camera = camera;
    }
}