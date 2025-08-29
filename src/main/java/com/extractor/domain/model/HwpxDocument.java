package com.extractor.domain.model;

import com.extractor.domain.vo.hwpx.HwpxImageVo;
import com.extractor.domain.vo.hwpx.HwpxSectionVo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;
import java.util.List;

@ToString
@Getter
public class HwpxDocument extends ExtractDocument {

    private List<HwpxSectionVo> sections;

    private List<HwpxImageVo> images;

    private Path unZipPath;

    @Builder
    public HwpxDocument(String docId, String name, List<HwpxSectionVo> sections, List<HwpxImageVo> images, Path path, Path unZipPath) {
        super(docId, name, path);
        this.sections = sections;
        this.images = images;
        this.unZipPath = unZipPath;
    }
}
