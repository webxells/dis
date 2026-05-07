/**
 * Copyright (C) 2020-2026 webXells GmbH
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 **/
package com.webxells.dis.officex.docx;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.officex.ReaderConfig;

@Description("Reading docx files by iterate over paragraphs." +
        "Path for content: content " +
        "Path of actual headings (current to title): heading-path" +
        "Current heading: heading " +
        "Current heading weight (title=0 till heading 7=7): heading-weight ")
public class ParagraphReaderConfig extends ReaderConfig {
    @Description("Merge content until a new heading is present")
    @Default("false")
    private boolean mergeContentByHeading;

    @Override
    public String getType() {
        return ParagraphReader.class.getName();
    }

    public boolean isMergeContentByHeading() {
        return mergeContentByHeading;
    }

    public void setMergeContentByHeading(final boolean mergeContentByHeading) {
        this.mergeContentByHeading = mergeContentByHeading;
    }
}