package com.catawiki.jira.prism;

import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class Code extends BaseMacro {

    private static final String FALLBACK_RENDER_OUTPUT = "{noformat}%s{noformat}";

    private final SoyTemplateRenderer soyTemplateRenderer;

    public Code(final SoyTemplateRendererProvider soyTemplateRendererProvider) {
        super();
        this.soyTemplateRenderer = soyTemplateRendererProvider.getRenderer();
    }

    @Override
    public boolean hasBody() {
        return true;
    }

    @Override
    public RenderMode getBodyRenderMode() {
        return RenderMode.allow(RenderMode.F_NONE);
    }

    @Override
    public String execute(Map<String, Object> parameters, String body, RenderContext renderContext) throws MacroException {

        ImmutableMap.Builder<String, Object> templateParams = ImmutableMap.builder();

        // Default language
        String language = "bsl";

        // Code body
        templateParams.put("content", body);

        // line numbers (optional)
        if (parameters.containsValue("linenumbers") || parameters.containsValue("ln")) {
            templateParams.put("linenumbers", true);
        }

        // wrap (optional)
        if (parameters.containsValue("wrap")) {
            templateParams.put("wrap", true);
        }

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            switch (entry.getKey()) {
                case "language":
                case "lang":
                    language = (String) entry.getValue();
                    break;
                case "title":
                    templateParams.put("title", (String) entry.getValue());
                    break;
                case "firstline":
                case "fl":
                case "start":
                    templateParams.put("start", (String) entry.getValue());
                    break;
                case "highlight":
                case "hl":
                    templateParams.put("highlight", (String) entry.getValue());
                    break;
                case "commandline":
                case "cmd":
                    String cmd = (String) entry.getValue();
                    String parts[] = cmd.split(">");
                    String prompt[] = parts[0].split("@");
                    String output = parts[1];
                    String user = prompt[0];
                    String host = prompt[1];
                    templateParams.put("commandline", true);
                    templateParams.put("user", user);
                    templateParams.put("host", host);
                    templateParams.put("output", output);
                    break;
                default:
            }
        }

        // Check for language as first parameter
        if (parameters.containsKey("0")) {
            language = (String) parameters.get("0");
        }
        templateParams.put("language", language);

        try {
            return this.soyTemplateRenderer.render(
                    "com.catawiki.jira.prism:soy",
                    "Prism.Macros.Code.html",
                    templateParams.build()
            );
        } catch (SoyException e) {
            return String.format(FALLBACK_RENDER_OUTPUT, body);
        }
    }
}
