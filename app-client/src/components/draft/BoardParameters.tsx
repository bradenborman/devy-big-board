import React from 'react';
import { useNavigate } from 'react-router-dom';

interface BoardParamsProps {
    teams: number;
    rounds: number;
    rookiesOnly: boolean;
    handleTeamsChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
    handleRoundsChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    handleRookiesOnlyChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    createGrid: () => void;
}

const BoardParameters: React.FC<BoardParamsProps> = ({ 
    teams, 
    rounds, 
    rookiesOnly,
    handleTeamsChange, 
    handleRoundsChange, 
    handleRookiesOnlyChange,
    createGrid 
}) => {
    const navigate = useNavigate();

    return (
        <div className="board-parameters">
            <div className="params-header">
                <button className="return-home-btn" onClick={() => navigate('/')}>
                    ← Home
                </button>
                <h3>{new Date().getUTCFullYear()} Devy/Rookie Draft Big Board</h3>
            </div>

            <div className="params-content">
                <div className="param-group">
                    <label>Teams</label>
                    <select value={teams} onChange={handleTeamsChange}>
                        <option value="6">6</option>
                        <option value="8">8</option>
                        <option value="10">10</option>
                        <option value="12">12</option>
                        <option value="14">14</option>
                        <option value="16">16</option>
                    </select>
                </div>

                <div className="param-group">
                    <label>Rounds: {rounds}</label>
                    <input
                        type="range"
                        value={rounds}
                        onChange={handleRoundsChange}
                        min="1"
                        max="7"
                        step="1"
                    />
                </div>

                <div className="param-group checkbox-group">
                    <label className="checkbox-label">
                        <input
                            type="checkbox"
                            checked={rookiesOnly}
                            onChange={handleRookiesOnlyChange}
                        />
                        <span>Rookies only?</span>
                    </label>
                    <small className="help-text">
                        Only show players from the current draft class ({new Date().getUTCFullYear()})
                    </small>
                </div>

                <button className="create-board-btn" onClick={createGrid}>
                    Create Big Board
                </button>
            </div>
        </div>
    );
};

export default BoardParameters;
