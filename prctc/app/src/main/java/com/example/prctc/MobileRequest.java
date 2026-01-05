
package com.example.prctc;
public class MobileRequest {
    public int battery_power, ram, internal_memory, camera;
    public float screen_size;

    public MobileRequest(int battery_power, int ram, int internal_memory, float screen_size, int camera) {
        this.battery_power = battery_power;
        this.ram = ram;
        this.internal_memory = internal_memory;
        this.screen_size = screen_size;
        this.camera = camera;
    }
}