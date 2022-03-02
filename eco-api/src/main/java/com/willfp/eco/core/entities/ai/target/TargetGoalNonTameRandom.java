package com.willfp.eco.core.entities.ai.target;

import com.willfp.eco.core.config.interfaces.Config;
import com.willfp.eco.core.entities.Entities;
import com.willfp.eco.core.entities.TestableEntity;
import com.willfp.eco.core.entities.ai.TargetGoal;
import com.willfp.eco.core.serialization.KeyedDeserializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Target random non-tame entity.
 *
 * @param targetClass     The types of entities to heal.
 * @param checkVisibility If visibility should be checked.
 * @param targetFilter    The filter for targets to match.
 */
public record TargetGoalNonTameRandom(
        @NotNull Class<? extends LivingEntity> targetClass,
        boolean checkVisibility,
        @NotNull Predicate<LivingEntity> targetFilter
) implements TargetGoal<Tameable> {
    /**
     * The deserializer for the goal.
     */
    public static final KeyedDeserializer<TargetGoalNonTameRandom> DESERIALIZER = new TargetGoalNonTameRandom.Deserializer();

    /**
     * Deserialize configs into the goal.
     */
    @SuppressWarnings("unchecked")
    private static final class Deserializer implements KeyedDeserializer<TargetGoalNonTameRandom> {
        @Override
        @Nullable
        public TargetGoalNonTameRandom deserialize(@NotNull final Config config) {
            if (!(
                    config.has("targetClass")
                            && config.has("checkVisibility")
                            && config.has("targetFilter")
            )) {
                return null;
            }

            try {
                TestableEntity filter = Entities.lookup(config.getString("targetFilter"));

                return new TargetGoalNonTameRandom(
                        (Class<? extends LivingEntity>)
                                Objects.requireNonNull(
                                        EntityType.valueOf(config.getString("avoidClass").toUpperCase()).getEntityClass()
                                ),
                        config.getBool("checkVisibility"),
                        filter::matches
                );
            } catch (Exception e) {
                /*
                Exceptions could be caused by configs having values of a wrong type,
                invalid enum parameters, etc. Serializers shouldn't throw exceptions,
                so we encapsulate them as null.
                 */
                return null;
            }
        }

        @NotNull
        @Override
        public NamespacedKey getKey() {
            return NamespacedKey.minecraft("non_tame_random");
        }
    }
}
