import React, { useRef } from 'react';
import { DraftStateMessage, PickMessage, ParticipantInfo } from '../../models/WebSocketMessages';
import './draft-complete-modal.scss';

interface Props {
  draftState: DraftStateMessage;
  onClose: () => void;
}

const DraftCompleteModal: React.FC<Props> = ({ draftState, onClose }) => {
  const printRef = useRef<HTMLDivElement>(null);

  const handlePrint = () => {
    window.print();
  };

  // Group picks by participant position
  const picksByParticipant = (participant: ParticipantInfo): PickMessage[] =>
    draftState.picks
      .filter((p) => p.pickedByPosition === participant.position)
      .sort((a, b) => a.pickNumber - b.pickNumber);

  return (
    <>
      {/* Modal overlay — hidden during print */}
      <div className="draft-complete-overlay no-print" onClick={onClose}>
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

          <div className="modal-tables" ref={printRef}>
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

          <div className="modal-actions no-print">
            <button className="btn-pdf" onClick={handlePrint}>
              🖨️ Save as PDF
            </button>
            <button className="btn-close" onClick={onClose}>
              Close
            </button>
          </div>
        </div>
      </div>

      {/* Print-only full layout */}
      <div className="print-only print-draft-sheet">
        <div className="print-header">
          <h1>Draft Results</h1>
          <p>{draftState.participants.length} Teams · {draftState.totalRounds} Rounds · {draftState.picks.length} Picks</p>
        </div>
        <div className="print-tables-grid">
          {draftState.participants.map((participant) => {
            const picks = picksByParticipant(participant);
            return (
              <div key={participant.position} className="print-participant">
                <h2>{participant.nickname}</h2>
                <table>
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
                        <td>{pick.position}</td>
                        <td>{pick.team || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
};

export default DraftCompleteModal;
