import React from 'react';
import { DraftStateMessage, PickMessage, ParticipantInfo } from '../../models/WebSocketMessages';
import './draft-complete-modal.scss';

interface Props {
  draftState: DraftStateMessage;
  onClose: () => void;
}

const DraftCompleteModal: React.FC<Props> = ({ draftState, onClose }) => {
  // Build columns from picks so we always have all teams even if participants array is partial
  const allPositions = Array.from({ length: draftState.participantCount }, (_, i) => String.fromCharCode(65 + i));
  const nicknameByPosition: Record<string, string> = {};
  draftState.participants.forEach(p => { nicknameByPosition[p.position] = p.nickname; });
  // Fall back to position letter if nickname not available
  const teamColumns = allPositions.map(pos => ({
    position: pos,
    nickname: nicknameByPosition[pos] || pos,
  }));

  const picksByPosition = (position: string): PickMessage[] =>
    draftState.picks
      .filter((p) => p.pickedByPosition === position)
      .sort((a, b) => a.pickNumber - b.pickNumber);

  const handlePrint = () => {
    const win = window.open('', '_blank', 'width=1100,height=800');
    if (!win) return;

    const posColors: Record<string, string> = {
      QB: '#7c3aed', RB: '#059669', WR: '#2563eb',
      TE: '#d97706', K: '#6b7280', DEF: '#374151', DST: '#374151',
    };

    const totalRounds = draftState.totalRounds;
    const participantCount = draftState.participantCount;

    // Build position->nickname map from participants we have
    const nicknameByPosition: Record<string, string> = {};
    draftState.participants.forEach(p => { nicknameByPosition[p.position] = p.nickname; });

    // Derive all positions from picks (covers cases where participants array is incomplete)
    const positionsFromPicks = Array.from(new Set(draftState.picks.map(p => p.pickedByPosition))).sort();
    // Fill any gaps up to participantCount using letter positions
    const allPositions = Array.from({ length: participantCount }, (_, i) => String.fromCharCode(65 + i));
    // Use positions that actually have picks, falling back to letter sequence
    const columns = allPositions.length >= positionsFromPicks.length ? allPositions : positionsFromPicks;

    // Build lookup: roundNumber -> pickedByPosition -> pick
    const pickMap: Record<number, Record<string, PickMessage>> = {};
    draftState.picks.forEach((pick) => {
      if (!pickMap[pick.roundNumber]) pickMap[pick.roundNumber] = {};
      pickMap[pick.roundNumber][pick.pickedByPosition] = pick;
    });

    const headerCells = columns.map(pos => {
      const name = nicknameByPosition[pos] || pos;
      return `<th>${name}</th>`;
    }).join('');

    const roundRows = Array.from({ length: totalRounds }, (_, i) => {
      const round = i + 1;
      const cells = columns.map(pos => {
        const pick = pickMap[round]?.[pos];
        if (!pick) return `<td class="empty-cell"></td>`;
        const color = posColors[pick.position] || '#555';
        return `
          <td>
            <div class="player-name">${pick.playerName}</div>
            <div class="pick-meta">
              <span class="pos-tag" style="background:${color}">${pick.position}</span>
              <span class="pick-num">Pick ${pick.pickNumber}</span>
            </div>
          </td>`;
      }).join('');
      return `<tr><td class="round-col">Rd ${round}</td>${cells}</tr>`;
    }).join('');

    win.document.write(`<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>Draft Results</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #111; padding: 20px; }
    .header { text-align: center; border-bottom: 2px solid #111; padding-bottom: 10px; margin-bottom: 16px; }
    h1 { font-size: 20pt; margin-bottom: 3px; }
    .subtitle { font-size: 9pt; color: #555; }
    table { width: 100%; border-collapse: collapse; font-size: 8pt; table-layout: fixed; }
    th { background: #222; color: #fff; padding: 6px 4px; text-align: center; font-size: 8.5pt; border: 1px solid #444; }
    td { padding: 5px 4px; border: 1px solid #ddd; vertical-align: top; }
    .round-col { background: #f0f0f0; font-weight: 700; text-align: center; width: 36px; color: #333; font-size: 7.5pt; }
    .player-name { font-weight: 600; font-size: 8pt; margin-bottom: 2px; }
    .pick-meta { display: flex; align-items: center; gap: 4px; }
    .pos-tag { color: #fff; padding: 1px 4px; border-radius: 3px; font-size: 7pt; font-weight: 700; }
    .pick-num { font-size: 7pt; color: #888; }
    .empty-cell { background: #fafafa; }
    tr:nth-child(even) td:not(.round-col) { background: #f9f9f9; }
    @media print { body { padding: 10px; } }
  </style>
</head>
<body>
  <div class="header">
    <h1>Draft Results</h1>
    <p class="subtitle">${participantCount} Teams &nbsp;·&nbsp; ${totalRounds} Rounds &nbsp;·&nbsp; ${draftState.picks.length} Picks</p>
  </div>
  <table>
    <thead><tr><th></th>${headerCells}</tr></thead>
    <tbody>${roundRows}</tbody>
  </table>
  <script>window.onload = function() { window.print(); }</script>
</body>
</html>`);
    win.document.close();
  };

  return (
    <div className="draft-complete-overlay" onClick={onClose}>
      <div className="draft-complete-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>🏆 Draft Complete!</h2>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>

        <div className="modal-summary">
          <span>{draftState.participantCount} teams</span>
          <span>·</span>
          <span>{draftState.totalRounds} rounds</span>
          <span>·</span>
          <span>{draftState.picks.length} picks</span>
        </div>

        <div className="modal-tables">
          {teamColumns.map(({ position, nickname }) => {
            const picks = picksByPosition(position);
            if (picks.length === 0) return null;
            return (
              <div key={position} className="participant-section">
                <h3 className="participant-name">{nickname}</h3>
                <table className="picks-table">
                  <thead>
                    <tr>
                      <th>Rd</th>
                      <th>Pick</th>
                      <th>Player</th>
                      <th>Pos</th>
                      <th>Team</th>
                    </tr>
                  </thead>
                  <tbody>
                    {picks.map((pick) => (
                      <tr key={pick.pickNumber}>
                        <td>{pick.roundNumber}</td>
                        <td>{pick.pickNumber}</td>
                        <td>{pick.playerName}</td>
                        <td>
                          <span className={`pos-badge ${pick.position}`}>{pick.position}</span>
                        </td>
                        <td>{pick.team || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            );
          })}
        </div>

        <div className="modal-actions">
          <button className="btn-pdf" onClick={handlePrint}>
            💾 Save Draft
          </button>
          <button className="btn-close" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default DraftCompleteModal;
