package com.colony.mod.client;

import com.colony.mod.ColonyMod;
import com.colony.mod.entity.ColonistEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class ColonistModel extends HumanoidModel<ColonistEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "colonist"),
            "main"
    );

    public ColonistModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.15f)),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "hat",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.45f)),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0f, 0.0f, -2.25f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.1f)),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 13.0f, 4.0f, CubeDeformation.NONE),
                PartPose.offset(-5.0f, 2.0f, 0.0f)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .mirror()
                        .texOffs(40, 16)
                        .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 13.0f, 4.0f, CubeDeformation.NONE)
                        .mirror(false),
                PartPose.offset(5.0f, 2.0f, 0.0f)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, CubeDeformation.NONE),
                PartPose.offset(-1.9f, 12.0f, 0.0f)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .mirror()
                        .texOffs(0, 16)
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, CubeDeformation.NONE)
                        .mirror(false),
                PartPose.offset(1.9f, 12.0f, 0.0f)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }
}
