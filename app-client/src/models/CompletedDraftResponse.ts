export interface DraftPick {
    draft_id: number;
    pickNumber: number;
    name: string;
    position: string;
    team: string;
}


export interface CompletedDraftResponse {
    id: number;
    created_at: string; // timestamp
    draft_date: string; // date only
    draft_time: string; // time only
    type: string;
    uuid: string;
    picks: DraftPick[];
}