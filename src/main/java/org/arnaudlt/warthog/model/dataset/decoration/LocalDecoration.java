package org.arnaudlt.warthog.model.dataset.decoration;

import org.arnaudlt.warthog.model.util.Format;

import java.util.List;

public record LocalDecoration(String basePath, List<String> parts, Format format, Double sizeInMegaBytes)
        implements Decoration {}
