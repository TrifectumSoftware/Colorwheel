package trifectumsoftware.colorwheel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
public class ColorwheelLateMixins implements ILateMixinLoader {

    private static final Logger LOG = LogManager.getLogger("colorwheel");

    @Override
    public String getMixinConfig() {
        return "mixins.colorwheel.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        if (loadedMods.contains("ArchitectureCraft")) {
            LOG.info("Colorwheel: ArchitectureCraft found, enabling shape tinting.");
            mixins.add("MixinRenderTargetWorld");
        }
        if (loadedMods.contains("hbm") && loadedMods.contains("IC2")) {
            LOG.info("Colorwheel: NTM and GT/IC2 found, enabling both charging systems.");
            mixins.add("MixinItemColorToolNTM");
        }
        return mixins;
    }
}
