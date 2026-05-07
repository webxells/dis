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
package com.webxells.dis.plain.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import java.util.Optional;

@Description("Deletes everything between two given strings (from & to)")
public class DeleteRange implements Manipulator {
    public enum NotFoundStrategy {
        @Description("Raises error") ERROR, @Description("Keeps data") IGNORE, @Description("Deletes whole value") BLANC
    }

    @Required(or = {"to"})
    @Description("Starting point of the range to delete")
    private String from;
    @Required(or = {"from"})
    @Description("End point of the range to delete")
    private String to;
    @Description("Deletes the found occurrences of the from- and to-string as well")
    @Default("false")
    private boolean deleteEdges;
    @Description("Uses index of last occurrence of the from string, otherwise the first one")
    @Default("false")
    private boolean lastIndex;
    @Description("Handles case that nothing was found between 'from' and 'to'")
    @Default("IGNORE")
    private NotFoundStrategy notFoundStrategy = NotFoundStrategy.IGNORE;

    @Override
    public void validate() throws InvalidApi {
        if (null == from && null == to) {
            throw new InvalidApi("At least one parameter is necessary ");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        currentPiece.rewriteValue(delete(currentPiece.value().orElse("")));
    }

    private String delete(final String content) throws InvalidDatasetException {
        final int length = content.length();
        final int start = Optional.ofNullable(from)
                .map(a -> (lastIndex ? content.lastIndexOf(a) : content.indexOf(a)) + (deleteEdges ? 0 : a.length()))
                .orElse(0);
        final int end = Optional.ofNullable(to)
                .map(a -> getEnd(a, content, start) + (deleteEdges ? a.length() : 0))
                .orElse(length);
        if (invalidStart(start) || invalidEnd(end, start)) {
            return handleNotFound(content);
        }
        return content.substring(0, start) + content.substring(end, length);
    }

    private int getEnd(final String a, final String content, final int start) {
        if (lastIndex) {
            final int lastIndex = content.lastIndexOf(a);
            return lastIndex <= start ? -1 : lastIndex;
        }
        return content.indexOf(a, start);
    }

    private boolean invalidEnd(final int end, final int start) {
        return end < start || (end == start && end == 0);
    }

    private boolean invalidStart(final int start) {
        return null != from && (start < 0 || (start == 0 && !deleteEdges));
    }

    private String handleNotFound(final String content) throws InvalidDatasetException {
        switch (notFoundStrategy) {
            case ERROR:
                throw new InvalidDatasetException("Could not find range: " + from + "; " + to);
            case IGNORE:
                return content;
        }
        return "";
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public void setTo(final String to) {
        this.to = to;
    }

    public void setNotFoundStrategy(final NotFoundStrategy notFoundStrategy) {
        this.notFoundStrategy = notFoundStrategy;
    }

    public void setDeleteEdges(final boolean deleteEdges) {
        this.deleteEdges = deleteEdges;
    }

    public void setLastIndex(final boolean lastIndex) {
        this.lastIndex = lastIndex;
    }
}
