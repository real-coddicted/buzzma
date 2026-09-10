package com.coddicted.buzzma.campaign.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.dto.CampaignDraftResponseDto;
import com.coddicted.buzzma.campaign.dto.CreateDraftRequestDto;
import com.coddicted.buzzma.campaign.dto.UpdateDraftRequestDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignDraft;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.persistence.CampaignDraftRepository;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignDraftServiceImplTest {

  private static final UUID DRAFT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID OWNER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID NON_OWNER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID CAMPAIGN_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final String GENERATED_CODE = "CAMP-XYZ";

  @Mock private CampaignDraftRepository mockCampaignDraftRepository;
  @Mock private CodeGenerationService mockCodeGenerationService;
  @Mock private CampaignService mockCampaignService;

  private CampaignDraftServiceImpl campaignDraftService;

  @BeforeEach
  void setUp() {
    this.campaignDraftService =
        new CampaignDraftServiceImpl(
            this.mockCampaignDraftRepository,
            this.mockCodeGenerationService,
            this.mockCampaignService);
  }

  @Test
  void testCreateStoresRequestFieldsWithGeneratedIdAndCode() {
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN))
        .thenReturn(GENERATED_CODE);
    final ArgumentCaptor<CampaignDraft> captor = ArgumentCaptor.forClass(CampaignDraft.class);
    when(this.mockCampaignDraftRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final CreateDraftRequestDto request =
        CreateDraftRequestDto.builder().title("My Draft").ownerId(OWNER_ID).build();

    final CampaignDraftResponseDto result = this.campaignDraftService.create(OWNER_ID, request);

    assertEquals("My Draft", result.getTitle());
    assertEquals(GENERATED_CODE, result.getCode());
    assertEquals(CampaignStatus.CAMPAIGN_STATUS_DRAFT, result.getStatus());
    assertEquals(OWNER_ID, result.getCreatedBy());
    assertEquals(result.getId(), captor.getValue().getId());
    assertEquals(GENERATED_CODE, captor.getValue().getCode());
  }

  @Test
  void testUpdateMergesOnlyProvidedFieldsLeavingOthersUnchanged() {
    final CampaignDraftResponseDto existingResponse =
        CampaignDraftResponseDto.builder()
            .id(DRAFT_ID)
            .code(GENERATED_CODE)
            .title("Old Title")
            .totalSlots(5)
            .build();
    final CampaignDraft existing =
        CampaignDraft.builder()
            .id(DRAFT_ID)
            .code(GENERATED_CODE)
            .responseJson(existingResponse)
            .build();
    when(this.mockCampaignDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(existing));
    final ArgumentCaptor<CampaignDraft> captor = ArgumentCaptor.forClass(CampaignDraft.class);
    when(this.mockCampaignDraftRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final UpdateDraftRequestDto request =
        UpdateDraftRequestDto.builder().title("New Title").build();

    final CampaignDraftResponseDto result =
        this.campaignDraftService.update(OWNER_ID, DRAFT_ID, request);

    assertEquals("New Title", result.getTitle());
    assertEquals(5, result.getTotalSlots());
    assertEquals(OWNER_ID, captor.getValue().getResponseJson().getUpdatedBy());
  }

  @Test
  void testUpdateWhenNotFoundThrows() {
    when(this.mockCampaignDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () ->
            this.campaignDraftService.update(
                OWNER_ID, DRAFT_ID, UpdateDraftRequestDto.builder().build()));
  }

  @Test
  void testGetByIdReturnsStoredResponseAsIs() {
    final CampaignDraftResponseDto response =
        CampaignDraftResponseDto.builder().id(DRAFT_ID).title("Stored Draft").build();
    final CampaignDraft existing =
        CampaignDraft.builder().id(DRAFT_ID).responseJson(response).build();
    when(this.mockCampaignDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(existing));

    final CampaignDraftResponseDto result = this.campaignDraftService.getById(DRAFT_ID);

    assertEquals(response, result);
  }

  @Test
  void testDeleteWhenOwnerSucceeds() {
    final CampaignDraftResponseDto response =
        CampaignDraftResponseDto.builder().id(DRAFT_ID).ownerId(OWNER_ID).build();
    final CampaignDraft existing =
        CampaignDraft.builder().id(DRAFT_ID).responseJson(response).build();
    when(this.mockCampaignDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(existing));

    this.campaignDraftService.delete(OWNER_ID, DRAFT_ID);

    verify(this.mockCampaignDraftRepository).delete(existing);
  }

  @Test
  void testDeleteWhenNotOwnerThrows() {
    final CampaignDraftResponseDto response =
        CampaignDraftResponseDto.builder().id(DRAFT_ID).ownerId(OWNER_ID).build();
    final CampaignDraft existing =
        CampaignDraft.builder().id(DRAFT_ID).responseJson(response).build();
    when(this.mockCampaignDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(existing));

    assertThrows(
        ForbiddenException.class, () -> this.campaignDraftService.delete(NON_OWNER_ID, DRAFT_ID));
  }

  @Test
  void testCopyFromCampaignBuildsDraftFromSourceCampaign() {
    final Product product =
        Product.builder()
            .name("Source Product")
            .brandName("Source Brand")
            .imageUrls(java.util.List.of())
            .build();
    final Campaign source =
        Campaign.builder()
            .id(CAMPAIGN_ID)
            .title("Source Campaign")
            .ownerId(OWNER_ID)
            .totalSlots(3)
            .product(product)
            .build();
    when(this.mockCampaignService.getById(CAMPAIGN_ID)).thenReturn(source);
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN))
        .thenReturn(GENERATED_CODE);
    final ArgumentCaptor<CampaignDraft> captor = ArgumentCaptor.forClass(CampaignDraft.class);
    when(this.mockCampaignDraftRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final CampaignDraftResponseDto result =
        this.campaignDraftService.copyFromCampaign(CAMPAIGN_ID, OWNER_ID);

    assertEquals("Source Campaign (Copy)", result.getTitle());
    assertEquals(GENERATED_CODE, result.getCode());
    assertEquals(CampaignStatus.CAMPAIGN_STATUS_DRAFT, result.getStatus());
    assertNull(result.getAssignees());
    assertEquals(OWNER_ID, result.getCreatedBy());
  }
}
