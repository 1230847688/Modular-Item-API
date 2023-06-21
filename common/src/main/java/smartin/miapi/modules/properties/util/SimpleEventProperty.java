package smartin.miapi.modules.properties.util;

import smartin.miapi.modules.properties.PotionEffectProperty;

/**
 * {@link SimpleEventProperty} is a class used to assist with {@link PropertyApplication.ApplicationEvent} handling.
 * @see PropertyApplication.ApplicationEvent
 * @see ApplicationEventHandler
 * @see PotionEffectProperty
 */
public abstract class SimpleEventProperty implements ApplicationEventHandler, ModuleProperty {
    private final EventHandlingMap<?> handlers;

    public SimpleEventProperty(EventHandlingMap<?> map) {
        handlers = map;
        for (PropertyApplication.ApplicationEvent<?> event : this.handlers.keySet()) {
            event.addListener(this);
        }
    }

    @Override
    public <E> void onEvent(PropertyApplication.ApplicationEvent<E> event, E instance) {
        handlers.get(event).accept(instance);
    }
}
