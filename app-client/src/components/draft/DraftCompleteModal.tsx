import React from 'react';
import { DraftStateMessage, PickMessage, ParticipantInfo } from '../../models/WebSocketMessages';
import './draft-complete-modal.scss';

interface Props {
  draftState: DraftStateMessage;
  onClose: () => void;
}

const DraftCompleteModal: React.FC<Props> = ({ draftState, onClose }) => {
  // Group picks by participant
  const picksByParticipant = (participant: ParticipantInfo): PickMessage[] =>
    draftState.picks
      .filter((p) => p.pickedByPosition === participant.position)
      .sort((a, b) => a.pickNumber - b.pickNumber);

  const handlePrint = () => {
    const win = window.open('', '_blank', 'width=900,height=700');
    if (!win) return;

    const posColors: Record<string, string> = {
      QB: '#7c3aed', RB: '#059669', WR: '#2563eb',
      TE: '#d97706', K: '#6b7280', DEF: '#374151', DST: '#374151',
    };

    const participantSections = draftState.participants.map((participant) => {
      const picks = picksByParticipant(participant);
      const rows = picks.map((pick) => {
        const color = posColors[pick.position] || '#555';
        return `
          <tr>
            <td>${pick.roundNumber}</td>
            <td>${pick.pickNumber}</td>
            <td>${pick.playerName}</td>
            <td><span style="background:${color};color:#fff;padding:2px 7px;border-radius:4px;font-size:8pt;font-weight:700">${pick.position}</span></td>
            <td>${pick.team || '—'}</td>
          </tr>`;
      }).join('');

      return `
        <div class="participant">
          <h2>${participant.nickname}</h2>
          <table>
            <thead><tr><th>Rd</th><th>Pick</th><th>Player</th><th>Pos</th><th>Team</th></tr></thead>
            <tbody>${rows}</tbody>
          </table>
        </div>`;
    }).join('');

    win.document.write(`<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>Draft Results</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #111; padding: 24px; }
    h1 { font-size: 22pt; margin-bottom: 4px; }
    .subtitle { font-size: 10pt; color: #555; margin-bottom: 20px; }
    .header { text-align: center; border-bottom: 2px solid #111; padding-bottom: 12px; margin-bottom: 20px; }
    .grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
    .participant { break-inside: avoid; }
    .participant h2 { font-size: 11pt; margin-bottom: 6px; padding-bottom: 4px; border-bottom: 1.5px solid #333; }
    table { width: 100%; border-collapse: collapse; font-size: 8.5pt; }
    th { text-align: left; padding: 3px 5px; border-bottom: 1px solid #999; font-weight: 600; color: #333; }
    td { padding: 3px 5px; border-bottom: 1px solid #ddd; }
    tr:nth-child(even) td { background: #f5f5f5; }
    @media print { body { padding: 16px; } }
  </style>
</head>
<body>
  <div class="header">
    <h1>Draft Results</h1>
    <p class="subtitle">${draftState.participants.length} Teams &nbsp;·&nbsp; ${draftState.totalRounds} Rounds &nbsp;·&nbsp; ${draftState.picks.length} Picks</p>
  </div>
  <div class="grid">${participantSections}</div>
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
          <span>{draftState.participants.length} teams</span>
          <span>·</span>
          <span>{draftState.totalRounds} rounds</span>
          <span>·</span>
          <span>{draftState.picks.length} picks</span>
        </div>

        <div className="modal-tables">
          {draftState.participants.map((participant) => {
            const picks = picksByParticipant(participant);
            return (
              <div key={participant.position} className="participant-section">
                <h3 className="participant-name">{participant.nickname}</h3>
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
            🖨️ Save as PDF
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
