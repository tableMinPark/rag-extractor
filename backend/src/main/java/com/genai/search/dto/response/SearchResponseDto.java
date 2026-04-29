package com.genai.search.dto.response;

import com.genai.search.repository.entity.DocumentEntity;
import com.genai.search.repository.wrapper.Search;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SearchResponseDto {

    private Long chunkId;
    private Long passageId;
    private Long sourceId;
    private Long fileDetailId;
    private String originFileName;
    private String name;
    private String title;
    private String subTitle;
    private String thirdTitle;
    private String compactContent;
    private String content;
    private String subContent;
    private String context;
    private String url;
    private String categoryCode;
    private String sourceType;
    private String ext;
    private String alias;
    private LocalDateTime sysCreateDt;
    private LocalDateTime sysModifyDt;
    private double score;

    public static SearchResponseDto from(Search<DocumentEntity> search) {
        DocumentEntity doc = search.getFields();
        return SearchResponseDto.builder()
                .chunkId(doc.getChunkId())
                .passageId(doc.getPassageId())
                .sourceId(doc.getSourceId())
                .fileDetailId(doc.getFileDetailId())
                .originFileName(doc.getOriginFileName())
                .name(doc.getName())
                .title(doc.getTitle())
                .subTitle(doc.getSubTitle())
                .thirdTitle(doc.getThirdTitle())
                .compactContent(doc.getCompactContent())
                .content(doc.getContent())
                .subContent(doc.getSubContent())
                .context(doc.getContext())
                .url(doc.getUrl())
                .categoryCode(doc.getCategoryCode())
                .sourceType(doc.getSourceType())
                .ext(doc.getExt())
                .alias(doc.getAlias())
                .sysCreateDt(doc.getSysCreateDt())
                .sysModifyDt(doc.getSysModifyDt())
                .score(search.getScore())
                .build();
    }

    public static List<SearchResponseDto> fromList(List<Search<DocumentEntity>> searches) {
        return searches.stream().map(SearchResponseDto::from).toList();
    }
}
