package org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.connectors.AbstractRendererConnector;
import com.vaadin.client.renderers.ClickableRenderer;
import com.vaadin.client.renderers.Renderer;
import com.vaadin.client.widget.grid.RendererCellReference;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.grid.renderers.RendererClickRpc;

import elemental.json.JsonObject;
import org.vaadin.grid.cellrenderers.client.editable.common.CellId;
import org.vaadin.grid.cellrenderers.client.editable.common.EditableRendererClientUtil;
import org.vaadin.grid.enhancements.cellrenderers.ComboBoxRenderer;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.EventHandler;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionsInfo;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;

import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
@Connect(ComboBoxRenderer.class)
public class ComboBoxRendererConnector extends AbstractRendererConnector<OptionElement> {
    private static final long serialVersionUID = 1L;

    ComboBoxServerRpc rpc = RpcProxy.create(ComboBoxServerRpc.class, this);

    public class ComboBoxRenderer extends ClickableRenderer<OptionElement, ComboBox> {

        private String filter = "";
        private boolean enabledValueChange = true;

        @Override
        public ComboBox createWidget() {
            final ComboBox comboBox = GWT.create(ComboBox.class);

            comboBox.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    event.stopPropagation();
                    final Element e = comboBox.getElement();
                    getRpcProxy(RendererClickRpc.class).click(e.getPropertyString(EditableRendererClientUtil.ROW_KEY_PROPERTY),
                                                              e.getPropertyString(EditableRendererClientUtil.COLUMN_ID_PROPERTY),
                                                              MouseEventDetailsBuilder.buildMouseEventDetails(event.getNativeEvent()));
                }
            }, ClickEvent.getType());

            comboBox.addDomHandler(new MouseDownHandler() {
                @Override
                public void onMouseDown(final MouseDownEvent event) {
                    event.stopPropagation();
                }
            }, MouseDownEvent.getType());

            comboBox.setEventHandler(new EventHandler<OptionElement>() {
                @Override
                public void change(final OptionElement item) {
                    if (org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect.ComboBoxRendererConnector.ComboBoxRenderer.this.enabledValueChange) {
                        ComboBoxRendererConnector.this.rpc.onValueChange(EditableRendererClientUtil.getCellId(comboBox), item);
                    }
                }

                @Override
                public void change(final Set<OptionElement> items) {
                    // NOOP
                }

                @Override
                public void getPage(final int pageNumber, final boolean skipBlur) {
                    if (!org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect.ComboBoxRendererConnector.ComboBoxRenderer.this.filter
                        .isEmpty()) {
                        ComboBoxRendererConnector.this.rpc.getFilterPage(
                                                                         org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect.ComboBoxRendererConnector.ComboBoxRenderer.this.filter,
                                                                         pageNumber, EditableRendererClientUtil.getCellId(comboBox));
                    }
                    else {
                        ComboBoxRendererConnector.this.rpc.getPage(pageNumber, EditableRendererClientUtil.getCellId(comboBox));
                    }
                }

                @Override
                public void filter(final String filterValue, final int pageNumber, final boolean skipBlur) {
                    org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect.ComboBoxRendererConnector.ComboBoxRenderer.this.filter = filterValue;
                    ComboBoxRendererConnector.this.rpc.getFilterPage(filterValue, pageNumber, EditableRendererClientUtil.getCellId(comboBox));
                }

                @Override
                public void clearFilter() {
                    org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect.ComboBoxRendererConnector.ComboBoxRenderer.this.filter = "";
                }

                @Override
                public void selectAll() {
                    // NOOP
                }

                @Override
                public void deselectAll() {
                    // NOOP
                }
            });

            registerRpc(ComboBoxClientRpc.class, new ComboBoxClientRpc() {
                private static final long serialVersionUID = 1L;

                @Override
                public void setCurrentPage(final int page, final CellId id) {
                    if (id.equals(EditableRendererClientUtil.getCellId(comboBox))) {
                        comboBox.setCurrentPage(page);
                    }
                }

                @Override
                public void updateOptions(final OptionsInfo optionsInfo, final List<OptionElement> options, final CellId id) {
                    if (id.equals(EditableRendererClientUtil.getCellId(comboBox))) {
                        if (optionsInfo.getCurrentPage() != -1) {
                            comboBox.setCurrentPage(optionsInfo.getCurrentPage());
                        }
                        comboBox.updatePageAmount(optionsInfo.getPageAmount());
                        comboBox.updateSelection(options);
                    }
                }

                @Override
                public void setEnabled(final boolean enabled, final CellId id) {
                    if (id.equals(EditableRendererClientUtil.getCellId(comboBox))) {
                        comboBox.setEnabled(enabled);
                    }
                }
            });

            comboBox.getPopup()
                .setOwner(EditableRendererClientUtil.getGridFromParent(getParent()));

            return comboBox;
        }

        @Override
        public void render(final RendererCellReference cell, final OptionElement selectedValue, final ComboBox comboBox) {
            this.filter = "";

            final Element e = comboBox.getElement();

            EditableRendererClientUtil.setElementProperties(e, getRowKey((JsonObject) cell.getRow()), EditableRendererClientUtil.getGridFromParent(getParent()),
                                                            getColumnId(EditableRendererClientUtil.getGridFromParent(getParent())
                                                                .getColumn(cell.getColumnIndex())));

            this.enabledValueChange = false;
            comboBox.setSelected(selectedValue);
            this.enabledValueChange = true;

            if (!cell.getColumn()
                .isEditable()
                    || !cell.getGrid()
                        .isEnabled()) {
                comboBox.setEnabled(false);
                return;
            }

            ComboBoxRendererConnector.this.rpc.onRender(new CellId(e.getPropertyString(EditableRendererClientUtil.ROW_KEY_PROPERTY),
                    e.getPropertyString(EditableRendererClientUtil.COLUMN_ID_PROPERTY)));
        }

    }

    @Override
    public ComboBoxState getState() {
        return (ComboBoxState) super.getState();
    }

    @Override
    protected Renderer<OptionElement> createRenderer() {
        return new ComboBoxRenderer();
    }

    @Override
    public ComboBoxRenderer getRenderer() {
        return (ComboBoxRenderer) super.getRenderer();
    }

}
