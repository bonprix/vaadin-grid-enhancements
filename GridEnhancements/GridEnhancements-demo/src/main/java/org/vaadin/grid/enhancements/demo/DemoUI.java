package org.vaadin.grid.enhancements.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.Renderer;
import org.vaadin.anna.gridactionrenderer.ActionGrid;
import org.vaadin.anna.gridactionrenderer.GridAction;
import org.vaadin.anna.gridactionrenderer.GridActionRenderer;
import org.vaadin.grid.cellrenderers.EditableRenderer;
import org.vaadin.grid.cellrenderers.editable.DateFieldRenderer;
import org.vaadin.grid.cellrenderers.editable.TextFieldRenderer;
import org.vaadin.grid.enhancements.cellrenderers.CheckBoxRenderer;
import org.vaadin.grid.enhancements.cellrenderers.ComboBoxMultiselectRenderer;
import org.vaadin.grid.enhancements.cellrenderers.ComboBoxRenderer;
import org.vaadin.grid.enhancements.navigation.GridNavigationExtension;
import org.vaadin.teemusa.gridextensions.client.tableselection.TableSelectionState;
import org.vaadin.teemusa.gridextensions.tableselection.TableSelectionModel;

import javax.servlet.annotation.WebServlet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@Theme("demo")
@Title("MyComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.vaadin.grid.enhancements.demo.DemoWidgetSet")
	public static class Servlet extends VaadinServlet {
	}

	private Label latestChangeLabel;

	@Override
	protected void init(VaadinRequest request) {

		this.latestChangeLabel = new Label("Latest change: -none-");

		final ActionGrid grid = new ActionGrid(createActions()) {
			@Override
			public void click(GridActionRenderer.GridActionClickEvent event) {
				DemoUI.this.latestChangeLabel.setValue("Latest change: '" + event	.getAction()
																					.getDescription()
						+ "'");
			}
		};
		grid.setContainerDataSource(new GeneratedPropertyContainer(getDataSource()));

		// Extend grid with navigation extension so we can navigate form input
		// to input
		GridNavigationExtension.extend(grid);

		final TableSelectionModel tableSelect = new TableSelectionModel();
		grid.setSelectionModel(tableSelect);
		tableSelect.setMode(TableSelectionState.TableSelectionMode.NONE);

		final HorizontalLayout tableSelectionControls = new HorizontalLayout();
		tableSelectionControls.setCaption("Table Selection Controls - NONE");

		// Controls for testing different TableSelectionModes
		for (final TableSelectionState.TableSelectionMode t : TableSelectionState.TableSelectionMode.values()) {
			tableSelectionControls.addComponent(new Button(t.toString(), new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					tableSelect.setMode(t);
					tableSelectionControls.setCaption("Table Selection Controls - " + t.toString());
				}
			}));
		}

		grid.setHeight("420px");
		grid.setWidth("1150px");

		// Add cell renderers
		// Custom action renderers
		grid.getColumn("actions")
			.setRenderer(grid.getGridActionRenderer());
		grid.getColumn("actions")
			.setWidth(100);

		// Field renderers
		grid.getColumn("foo")
			.setRenderer(new TextFieldRenderer<String>());
		grid.getColumn("foo")
			.setExpandRatio(1);
		grid.getColumn("bar")
			.setRenderer(new TextFieldRenderer<Integer>());
		grid.getColumn("bar")
			.setWidth(100);
		grid.getColumn("km")
			.setRenderer(new TextFieldRenderer<Double>());
		grid.getColumn("km")
			.setWidth(100);
		grid.getColumn("today")
			.setRenderer(new DateFieldRenderer());
		grid.getColumn("today")
			.setWidth(150);
		grid.getColumn("yes")
			.setRenderer(new CheckBoxRenderer());
		grid.getColumn("yes")
			.setWidth(65);

		// ComboBox renderers
		((GeneratedPropertyContainer) grid.getContainerDataSource()).addGeneratedProperty(	"_multi",
																							new PropertyValueGenerator<DummyClass>() {

																								@Override
																								public DummyClass getValue(
																										Item item,
																										Object itemId,
																										Object propertyId) {
																									return new DummyClass();
																								}

																								@Override
																								public Class<DummyClass> getType() {
																									return DummyClass.class;
																								}
																							});
		grid.getColumn("_multi")
			.setRenderer(new ComboBoxMultiselectRenderer<DummyClass>(DummyClass.class, getItemList(), "id", "name", 10,
					"input prompt2", "select all2", "clear2"));
		grid.getColumn("_multi")
			.setWidth(400);
		grid.getColumn("multi")
			.setWidth(150);
		grid.getColumn("single")
			.setRenderer(new ComboBoxRenderer<DummyClass>(DummyClass.class, getItemList(), "id", "name", 5,
					"input prompt", "select all", "clear"));
		grid.getColumn("single")
			.setWidth(150);

		// Add renderer listeners so we catch item edit events.
		for (Grid.Column col : grid.getColumns()) {
			Renderer<?> renderer = col.getRenderer();
			if (!(renderer instanceof EditableRenderer))
				continue;

			// In the demo instance we want to show a formatted date
			if (renderer.getPresentationType()
						.equals(Date.class)) {
				((EditableRenderer) renderer).addItemEditListener(this.dateItemEdit);
			} else {
				((EditableRenderer) renderer).addItemEditListener(this.itemEdit);
			}
		}

		// Show it in the middle of the screen
		VerticalLayout content = new VerticalLayout();
		content.setStyleName("demoContentLayout");
		content.setSizeFull();
		setContent(content);

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setSizeUndefined();
		layout.addComponent(grid);
		layout.addComponent(this.latestChangeLabel);
		layout.addComponent(tableSelectionControls);

		content.addComponent(layout);
		content.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);

		grid.setColumnOrder("_multi");
	}

	public class DummyClass {
		private Long id;
		private String name;

		public DummyClass() {
		}

		public DummyClass(Long id, String name) {
			this.id = id;
			this.name = name;
		}

		public Long getId() {
			return this.id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DummyClass)) {
				return false;
			}

			DummyClass otherDummyClass = (DummyClass) obj;
			if (otherDummyClass.getId() == null) {
				return false;
			}

			return otherDummyClass	.getId()
									.equals(this.getId());
		}

		@Override
		public int hashCode() {
			if (getId() == null) {
				return 0;
			}
			return (int) (getId() ^ (getId() >>> 32));
		}

		@Override
		public String toString() {
			return getName();
		}

	}

	private LinkedList<DummyClass> getItemList() {
		return new LinkedList<DummyClass>() {
			{
				add(new DummyClass(1L, "one"));
				add(new DummyClass(2L, "two"));
				add(new DummyClass(3L, "three"));
				add(new DummyClass(4L, "four"));
				add(new DummyClass(5L, "five"));
				add(new DummyClass(6L, "six"));
				add(new DummyClass(7L, "seven"));
				add(new DummyClass(8L, "eight"));
				add(new DummyClass(9L, "nine"));
				add(new DummyClass(10L, "ten"));
			}
		};
	}

	private static List<GridAction> createActions() {
		List<GridAction> actions = new ArrayList<GridAction>();
		actions.add(new GridAction(FontAwesome.USER, "user"));
		actions.add(new GridAction(FontAwesome.GEAR, "settings"));
		return actions;
	}

	/**
	 * Create and populate an indexed container
	 *
	 * @return Populated indexed container
	 */
	public BeanItemContainer<DataSourceClass> getDataSource() {
		BeanItemContainer<DataSourceClass> container = new BeanItemContainer<DataSourceClass>(DataSourceClass.class);

		// Populate data
		for (int i = 0; i <= 30; ++i) {
			container.addBean(new DataSourceClass("0,1", "foo", i, i / 5.0d, new Date(), false,
					new DummyClass(2L, "two"), new HashSet<DummyClass>() {
						{
							add(new DummyClass(1L, "one"));
							add(new DummyClass(8L, "eight"));
						}
					}));
		}

		return container;
	}

	// --- ItemEditListeners ---
	/**
	 * Update change lable with the column and value of the latest edit
	 */
	private EditableRenderer.ItemEditListener<DataSourceClass> itemEdit = new EditableRenderer.ItemEditListener<DataSourceClass>() {
		@Override
		public void itemEdited(EditableRenderer.ItemEditEvent<DataSourceClass> event) {
			DemoUI.this.latestChangeLabel.setValue("Latest change: '" + event.getColumnPropertyId() + "' "
					+ (EditableRenderer.Mode.SINGLE.equals(event.getMode()) ? event.getNewValue()
							: event.getNewValues()));
		}
	};

	/**
	 * Same as itemEdit, but with the date value formatted.
	 */
	private EditableRenderer.ItemEditListener<DataSourceClass> dateItemEdit = new EditableRenderer.ItemEditListener<DataSourceClass>() {
		@Override
		public void itemEdited(EditableRenderer.ItemEditEvent<DataSourceClass> event) {
			DemoUI.this.latestChangeLabel.setValue("Latest change: '" + event.getColumnPropertyId() + "' "
					+ new SimpleDateFormat("dd-MM-yyyy").format(event.getNewValue()));
		}
	};

	public class DataSourceClass {

		// container.addContainerProperty("actions", String.class, "0,1");
		private String actions;
		private String foo;
		private Integer bar;
		private Double km;
		private Date today;
		private Boolean yes;
		private DummyClass single;
		private HashSet<DummyClass> multi;

		public DataSourceClass() {
		}

		public DataSourceClass(String actions, String foo, Integer bar, Double km, Date today, Boolean yes,
				DummyClass single, HashSet<DummyClass> multi) {
			this.actions = actions;
			this.foo = foo;
			this.bar = bar;
			this.km = km;
			this.today = today;
			this.yes = yes;
			this.single = single;
			this.multi = multi;
		}

		public String getActions() {
			return this.actions;
		}

		public void setActions(String actions) {
			this.actions = actions;
		}

		public String getFoo() {
			return this.foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}

		public Integer getBar() {
			return this.bar;
		}

		public void setBar(Integer bar) {
			this.bar = bar;
		}

		public Double getKm() {
			return this.km;
		}

		public void setKm(Double km) {
			this.km = km;
		}

		public Date getToday() {
			return this.today;
		}

		public void setToday(Date today) {
			this.today = today;
		}

		public Boolean getYes() {
			return this.yes;
		}

		public void setYes(Boolean yes) {
			this.yes = yes;
		}

		public DummyClass getSingle() {
			return this.single;
		}

		public void setSingle(DummyClass single) {
			this.single = single;
		}

		public HashSet<DummyClass> getMulti() {
			return this.multi;
		}

		public void setMulti(HashSet<DummyClass> multi) {
			this.multi = multi;
		}

	}
}
