package wild.core.nms.v1_8_R3;

import net.minecraft.server.v1_8_R3.EntityFireworks;
import net.minecraft.server.v1_8_R3.World;

public class SilentEntityFirework extends EntityFireworks {

    public SilentEntityFirework(World world) {
        super(world);
        setInvisible(true);
    }

    @Override
    public void t_() {
        world.broadcastEntityEffect(this, (byte) 17);
        this.die();
    }
}