package com.example.prctc;

public class LaptopRequest {
    public int ram, storage_ssd, graphics;
    public float processor_speed, screen_size;

    public LaptopRequest(int ram, int storage_ssd, float processor_speed, float screen_size, int graphics) {
        this.ram = ram;
        this.storage_ssd = storage_ssd;
        this.processor_speed = processor_speed;
        this.screen_size = screen_size;
        this.graphics = graphics;
    }
}