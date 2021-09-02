package net.runelite.client.plugins.bodfishing;


import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.client.plugins.xptracker.XpTrackerService;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Slf4j
@Singleton
class BodFishingOverlay extends OverlayPanel
{
	private final Client client;
	private final BodFishingPlugin plugin;
	private final BodFishingConfig config;
	private final XpTrackerService xpTrackerService;

	String timeFormat;

	@Inject
	BodFishingOverlay(final Client client, final BodFishingPlugin plugin, final BodFishingConfig config, XpTrackerService xpTrackerService)
	{
		super(plugin);
		this.xpTrackerService = xpTrackerService;
		setPosition(OverlayPosition.BOTTOM_RIGHT);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "BodFishing overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.startedTimestamp == null || !config.enableOverlay() || !plugin.startScript)
		{
			return null;
		}

		// status
		TableComponent statusComponent = getTableComponent();
		Duration duration = Duration.between(plugin.startedTimestamp, Instant.now());
		timeFormat = (duration.toHours() < 1) ? "mm:ss" : "HH:mm:ss";
		statusComponent.addRow("Time:", formatDuration(duration.toMillis(), timeFormat));
		statusComponent.addRow("Status:", plugin.getStatus());

		// stats
		TableComponent statsComponent = getTableComponent();
		int actions = xpTrackerService.getActions(Skill.FISHING);
		statsComponent.addRow("Caught:", actions + " (" + xpTrackerService.getActionsHr(Skill.FISHING) + "/hr)");

		if (!statusComponent.isEmpty())
		{
			panelComponent.setBackgroundColor(new Color(70, 61, 50, 156));
			panelComponent.setPreferredSize(new Dimension(150, 100));
			panelComponent.setBorder(new Rectangle(5, 5, 5, 5));

			// status
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Bod Fisherman")
				.color(ColorUtil.fromHex("#40C4FF"))
				.build());
			panelComponent.getChildren().add(statusComponent);

			// stats
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Stats")
				.color(ColorUtil.fromHex("#FFA000"))
				.build());
			panelComponent.getChildren().add(statsComponent);

		}

		return super.render(graphics);
	}

	private TableComponent getTableComponent()
	{
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		return tableComponent;
	}
}
