package com.github.hololand.objecttweaker;
import java.util.Objects;

public class GameObject {
    public String Name;
    private double posX, posY, posZ; // posZ = Elevation(Relative)
    private double yaw, pitch, roll, scale;

    public GameObject(String objectString){
        String[] rawData = objectString.split(";");
        Name = rawData[0].substring(1,rawData[0].length()-1);
        posX = Double.parseDouble(rawData[1]);
        posY = Double.parseDouble(rawData[2]);
        posZ = Double.parseDouble(rawData[7]);
        yaw = Double.parseDouble(rawData[3]);
        pitch = Double.parseDouble(rawData[4]);
        roll = Double.parseDouble(rawData[5]);
        scale = Double.parseDouble(rawData[6]);
    }
    // GETTERS
    public double getPosX(){
        return this.posX;
    }
    public double getPosY(){
        return this.posY;
    }
    public double getPosZ(){
        return this.posZ;
    }
    public double getYaw(){
        return this.yaw;
    }
    public double getPitch(){
        return this.pitch;
    }
    public double getRoll(){
        return this.roll;
    }
    public double getScale(){
        return this.scale;
    }
    // SETTERS
    public void setPosX(double setPosX){ posX = setPosX; }
    public void setPosY(double setPosY){
        posY = setPosY;
    }
    public void setPosZ(double setPosZ){
        posZ = setPosZ;
    }
    public void setYaw(double setYaw){
        yaw = setYaw;
    }
    public void setPitch(double setPitch){
        pitch = setPitch;
    }
    public void setRoll(double setRoll){
        roll = setRoll;
    }
    public void setScale(double setScale){
        scale = setScale;
    }
    // Overriding Hashset and equals for more efficient duplicate detection
    @Override
    public int hashCode() {
        return Objects.hash(Name,
                String.format("%.1f", posX),
                String.format("%.1f", posY),
                String.format("%.1f", posZ),
                String.format("%.1f", yaw),
                String.format("%.1f", pitch),
                String.format("%.1f", roll),
                String.format("%.1f", scale)
                );
//        long posXLong = Double.doubleToLongBits(posX);
//        long posYLong = Double.doubleToLongBits(posY);
//        long posZLong = Double.doubleToLongBits(posZ);
//        long yawLong = Double.doubleToLongBits(yaw);
//        long pitchLong = Double.doubleToLongBits(pitch);
//        long rollLong = Double.doubleToLongBits(roll);
//        long scaleLong = Double.doubleToLongBits(scale);
//        int result = 17;
//        result = 31 * result + Name.hashCode();
//        result = 31 * result + (int) (posXLong ^ (posXLong >>> 32));
//        result = 31 * result + (int) (posYLong ^ (posYLong >>> 32));
//        result = 31 * result + (int) (posZLong ^ (posZLong >>> 32));
//        result = 31 * result + (int) (yawLong ^ (yawLong >>> 32));
//        result = 31 * result + (int) (pitchLong ^ (pitchLong >>> 32));
//        result = 31 * result + (int) (rollLong ^ (rollLong >>> 32));
//        result = 31 * result + (int) (scaleLong ^ (scaleLong >>> 32));
//        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GameObject)) {
            return false;
        }
        GameObject o = (GameObject) obj;
        return this.Name.equals(o.Name)
                && String.format("%.1f", this.posX).equals(String.format("%.1f", o.posX))
                && String.format("%.1f", this.posY).equals(String.format("%.1f", o.posY))
                && String.format("%.1f", this.posZ).equals(String.format("%.1f", o.posZ))
                && String.format("%.1f", this.yaw).equals(String.format("%.1f", o.yaw))
                && String.format("%.1f", this.pitch).equals(String.format("%.1f", o.pitch))
                && String.format("%.1f", this.roll).equals(String.format("%.1f", o.roll))
                && String.format("%.1f", this.scale).equals(String.format("%.1f", o.scale));
    }
}
