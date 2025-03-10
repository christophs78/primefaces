# Message

Message is a pre-skinned extended version of the standard Jakarta Faces message component.

[See this widget in the JavaScript API Docs.](../jsdocs/classes/src_PrimeFaces.PrimeFaces.widget.Message.html)

## Info

| Name | Value |
| --- | --- |
| Tag | message
| Component Class | org.primefaces.component.message.Message
| Component Type | org.primefaces.component.Message
| Component Family | org.primefaces.component |
| Renderer Type | org.primefaces.component.MessageRenderer
| Renderer Class | org.primefaces.component.message.MessageRenderer

## Attributes

| Name | Default | Type | Description |
| --- | --- | --- | --- |
id | null | String | Unique identifier of the component.
rendered | true | Boolean | Boolean value to specify the rendering of the component, when set to false component will not be rendered.
binding | null | Object | An el expression that maps to a server side UIComponent instance in a backing bean.
widgetVar | null | String | Name of the client side widget.
showSummary | false | Boolean | Specifies if the summary of the FacesMessage should be displayed.
showDetail | true | Boolean | Specifies if the detail of the FacesMessage should be displayed.
for | null | String | The clientId or name of associated key of whose messages to display.
redisplay | true | Boolean | Defines if already rendered messages should be displayed
display | both | String | Defines the display mode.
escape | true | Boolean | Defines whether HTML would be escaped or not.
severity | null | String | Comma separated list of severities to display only.
style | null | String | Inline style of the component.
styleClass | null | String | Style class of the component.
skipDetailIfEqualsSummary | false | Boolean | Defines if rendering of the detail text should be skipped, if the detail and summary are equals.

## Getting started with Message
Message usage is exactly same as standard message.

```xhtml
<h:inputText id="txt" value="#{bean.text}" />
<p:message for="txt" />
```

## Display Mode
Message component has four different display modes;

- text : Only message text is displayed.
- icon : Only message severity is displayed and message text is visible as a tooltip.
- both (default) : Both icon and text are displayed.
- tooltip : Message text is visible as a tooltip of the target component.

## Severity Levels
Using severity attribute, you can define which severities can be displayed by the component. For
instance, you can configure messages to only display infos and warnings.

```xhtml
<p:message severity="info,warn" for="txt"/>
```

## Escaping
Component escapes HTML content in messages by default, in case you need to display HTML, disable
escape option.

```xhtml
<p:message escape="false" for="txt" />
```

## Client Side API
Widget: _PrimeFaces.widget.Messages_

| Method               | Params | Return Type | Description                |
|----------------------| --- | --- |----------------------------|
| render(facesMessage) | facesMessage: see JSDoc about PrimeFaces.FacesMessage  | void | renders the message        |
| clear()              | none  | void | clears the current message |

## Skinning
Full list of CSS selectors of message is as follows;

| Class | Applies |
| --- | --- |
ui-message-{severity} | Container element of the message
ui-message-{severity}-summary | Summary text
ui-message-{severity}-detail | Detail text
ui-message-{severity}-icon | Icon of the message

{severity} can be `info`, `warn`, `error` and `fatal`.

