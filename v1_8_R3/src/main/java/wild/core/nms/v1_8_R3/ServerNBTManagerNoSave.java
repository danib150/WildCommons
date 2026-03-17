package wild.core.nms.v1_8_R3;

import java.io.File;

import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.ServerNBTManager;


public class ServerNBTManagerNoSave extends ServerNBTManager {

    public ServerNBTManagerNoSave(File file, String s, boolean flag) {
        super(file, s, flag);
    }

    @Override
    public void save(EntityHuman entityhuman) {
    }

    @Override
    public NBTTagCompound load(EntityHuman entityhuman) {
        return null;
    }

    @Override
    public NBTTagCompound getPlayerData(String s) {
        return null;
    }
}