package com.gallantrealm.modsynth;

/**
 * This listener provides a single method, clientModelChanged, that is invoked on any change to the client model. The
 * ClientModelChangedEvent event can be used to get more information on the type of model change.
 */
public interface ClientModelChangedListener {
	public void clientModelChanged(ClientModelChangedEvent event);
}
