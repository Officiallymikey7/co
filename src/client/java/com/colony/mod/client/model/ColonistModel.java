package com.colony.mod.client.model;

import com.colony.mod.ColonyMod;
import com.colony.mod.entity.ColonistEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Custom colonist geometry and animation controller.
 */
public class ColonistModel extends HierarchicalModel<ColonistEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ColonyMod.MOD_ID, "colonist"),
            "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ColonistModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild(PartNames.HEAD);
        this.hat = root.getChild(PartNames.HAT);
        this.body = root.getChild(PartNames.BODY);
        this.rightArm = root.getChild(PartNames.RIGHT_ARM);
        this.leftArm = root.getChild(PartNames.LEFT_ARM);
        this.rightLeg = root.getChild(PartNames.RIGHT_LEG);
        this.leftLeg = root.getChild(PartNames.LEFT_LEG);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild(
                PartNames.HEAD,
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        root.addOrReplaceChild(
                PartNames.HAT,
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        root.addOrReplaceChild(
                PartNames.BODY,
                CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.ZERO);
        root.addOrReplaceChild(
                PartNames.RIGHT_ARM,
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild(
                PartNames.LEFT_ARM,
                CubeListBuilder.create().texOffs(40, 16).mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild(
                PartNames.RIGHT_LEG,
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild(
                PartNames.LEFT_LEG,
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(ColonistEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        animateHead(netHeadYaw, headPitch);
        animateWalk(limbSwing, limbSwingAmount);
        animateIdle(ageInTicks);
    }

    private void animateHead(float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);
        this.hat.yRot = this.head.yRot;
        this.hat.xRot = this.head.xRot;
    }

    private void animateWalk(float limbSwing, float limbSwingAmount) {
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.0F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
    }

    private void animateIdle(float ageInTicks) {
        float breathing = Mth.sin(ageInTicks * 0.09F) * 0.05F;
        this.body.xRot = breathing * 0.2F;
        this.rightArm.zRot = breathing + 0.05F;
        this.leftArm.zRot = -breathing - 0.05F;
        this.head.y += breathing * 0.5F;
        this.hat.y += breathing * 0.5F;
        this.body.y += breathing * 0.25F;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
