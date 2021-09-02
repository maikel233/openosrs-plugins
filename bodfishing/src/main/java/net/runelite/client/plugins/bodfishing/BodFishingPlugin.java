package net.runelite.client.plugins.bodfishing;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.time.Instant;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bodfishing.enums.FishingChoice;
import net.runelite.client.plugins.bodfishing.enums.TickManipulation;
import net.runelite.client.plugins.bodfishing.states.FishingState;
import net.runelite.client.plugins.bodfishing.states.ProcessItemState;
import net.runelite.client.plugins.bodfishing.states.State;
import net.runelite.client.plugins.bodfishing.states.StateSet;
import net.runelite.client.plugins.bodfishing.states.WalkToFishingState;
import net.runelite.client.plugins.paistisuite.PScript;
import net.runelite.client.plugins.paistisuite.PaistiSuite;
import net.runelite.client.plugins.paistisuite.api.PPlayer;
import net.runelite.client.plugins.paistisuite.api.PUtils;
import net.runelite.client.plugins.paistisuite.api.PWalking;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.DaxWalker;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDependency(PaistiSuite.class)
@PluginDependency(XpTrackerPlugin.class)
@PluginDescriptor(
	name = "Bod AIO Fisherman",
	enabledByDefault = false,
	description = "Dadbod - AIO Fisher",
	tags = {"banking", "items", "paisti", "dadbod", "fishing", "tick"}
)

@Slf4j
@Singleton
public class BodFishingPlugin extends PScript
{
	public boolean startScript = false;
	public Instant startedTimestamp;
	public boolean enableTickManipulation = false;
	public TickManipulation tickManipulation = TickManipulation.TEAK_KNIFE;
	public FishingChoice fishingChoice = FishingChoice.BARBARIAN_OUTPOST;
	public boolean bankFishChoice = false;
	public boolean bankCookedFishChoice = false;
	public boolean cookedFishChoice = false;
	public boolean dropClueScrolls = false;

	public int minSleepBeforeNewSpot;
	public int maxSleepBeforeNewSpot;
	public int minSleepBefore3t;
	public int maxSleepBefore3t;
	public int minSleepBeforeDrop;
	public int maxSleepBeforeDrop;

	private StateSet<BodFishingPlugin> states = new StateSet<>();
	State<BodFishingPlugin> currentState;

	public WalkToFishingState walkToFishingState = new WalkToFishingState(this);
	public FishingState fishingState = new FishingState(this);
	public ProcessItemState processItemState = new ProcessItemState(this);

	@Getter
	@Setter
	private String status;

	@Inject
	private BodFishingConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private BodFishingOverlay overlay;
	@Inject
	private ConfigManager configManager;

	@Provides
	BodFishingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BodFishingConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (PUtils.getClient() != null && PUtils.getClient().getGameState() == GameState.LOGGED_IN)
		{
			readConfig();
		}
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		requestStop();
		startScript = false;
		overlayManager.remove(overlay);
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			readConfig();
		}
	}

	private synchronized void readConfig()
	{
		enableTickManipulation = config.enableTickManipulation();
		tickManipulation = config.tickManipulationChoice();
		fishingChoice = config.fishingChoice();
		bankFishChoice = config.bankFishChoice();
		bankCookedFishChoice = config.bankCookedFishChoice();
		dropClueScrolls = config.dropClueScrolls();
		cookedFishChoice = config.cookedFishChoice();

		minSleepBeforeNewSpot = config.minSleepBeforeNewSpot();
		maxSleepBeforeNewSpot = config.maxSleepBeforeNewSpot();
		minSleepBefore3t = config.minSleepBefore3t();
		maxSleepBefore3t = config.maxSleepBefore3t();
		minSleepBeforeDrop = config.minSleepBeforeDrop();
		maxSleepBeforeDrop = config.maxSleepBeforeDrop();
	}

	private synchronized void loadStates()
	{
		states.clear();
		states.addAll(
			this.walkToFishingState,
			this.processItemState,
			this.fishingState
		);
	}

	@Override
	protected synchronized void onStart()
	{
		PUtils.sendGameMessage("BodFishing started!");
		startedTimestamp = Instant.now();
		readConfig();
		DaxWalker.setCredentials(PaistiSuite.getDaxCredentialsProvider());
		DaxWalker.getInstance().allowTeleports = false;
		loadStates();
	}

	@Override
	protected synchronized void onStop()
	{
		PUtils.sendGameMessage("BodFishing stopped!");
		startedTimestamp = null;
		startScript = false;
	}

	@Subscribe
	private synchronized void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equalsIgnoreCase("BodFishing"))
		{
			return;
		}
		readConfig();
		PUtils.sleepNormal(400, 800);
		PWalking.sceneWalk(PPlayer.getWorldLocation());
	}

	@Subscribe
	private synchronized void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("BodFishing"))
		{
			return;
		}
		if (PPlayer.get() == null && PUtils.getClient().getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (configButtonClicked.getKey().equals("startButton"))
		{
			try
			{
				super.start();
				startScript = true;
			}
			catch (Exception e)
			{
				log.error(e.toString());
				e.printStackTrace();
				startScript = false;
			}
		}
		else if (configButtonClicked.getKey().equals("stopButton"))
		{
			requestStop();
			startScript = false;
			if (enableTickManipulation)
			{
				PUtils.sleepNormal(400, 800);
				PWalking.sceneWalk(PPlayer.getWorldLocation());
			}
		}
	}

	@Override
	protected void loop()
	{
		PUtils.sleepFlat(50, 150);
		if (playerNotReady())
		{
			return;
		}

		updateState();
		if (currentState != null)
		{
			currentState.loop();
		}
	}

	@Subscribe
	protected void onAnimationChanged(AnimationChanged event)
	{
		if (!event.getActor().equals(PPlayer.getPlayer()))
		{
			return;
		}

		if (playerNotReady())
		{
			return;
		}

		states.eachAnimationEvent(event);
	}

	private void updateState()
	{
		currentState = states.getValidState();
		if (currentState != null)
		{
			setStatus(currentState.getName());
		}
		else
		{
			setStatus("?");
		}
	}

	private boolean playerNotReady()
	{
		return !startScript || PUtils.getClient().getGameState() != GameState.LOGGED_IN || isStopRequested();
	}

}
