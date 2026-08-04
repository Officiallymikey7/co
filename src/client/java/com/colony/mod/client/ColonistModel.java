package com.colony.mod.client;

import com.colony.mod.entity.ColonistEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Custom model for the {@link ColonistEntity}.
 *
 * <p>Uses standard humanoid proportions on a 64×64 texture sheet.
 * The geometry mirrors the vanilla player model so that the existing
 * {@code colony:textures/entity/colonist.png} texture lines up correctly.
 * Swap out cube sizes / UV offsets here whenever you want a unique silhouette.
 */
public class ColonistModel extends HumanoidModel<ColonistEntity> {

    public ColonistModel(ModelPart root) {
        super(root);
    }

    /**
     * Builds the {@link LayerDefinition} that describes the colonist's geometry.
     * Called once during client initialisation and the result is baked into a
     * {@link ModelPart} tree that is passed to {@link #ColonistModel(ModelPart)}.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Head — 8×8×8, UV origin (0, 0)
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        // Hat overlay — slightly inflated head, UV origin (32, 0)
        root.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, new CubeDeformation(0.5f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        // Body — 8×12×4, UV origin (16, 16)
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0f, 0.0f, -2.0f, 8, 12, 4, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        // Right arm — 4×12×4, UV origin (40, 16)
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-3.0f, -2.0f, -2.0f, 4, 12, 4, new CubeDeformation(0.0f)),
                PartPose.offset(-5.0f, 2.0f, 0.0f));

        // Left arm — 4×12×4, UV origin (32, 48)
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(32, 48)
                        .addBox(-1.0f, -2.0f, -2.0f, 4, 12, 4, new CubeDeformation(0.0f)),
                PartPose.offset(5.0f, 2.0f, 0.0f));

        // Right leg — 4×12×4, UV origin (0, 16)
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, new CubeDeformation(0.0f)),
                PartPose.offset(-1.9f, 12.0f, 0.0f));

        // Left leg — 4×12×4, UV origin (16, 48)
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(16, 48)
                        .addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, new CubeDeformation(0.0f)),
                PartPose.offset(1.9f, 12.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
