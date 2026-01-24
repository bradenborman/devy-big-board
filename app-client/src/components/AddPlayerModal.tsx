import React, { useState } from 'react';
import { Player } from './bigBoard';
import './addPlayerModal.scss';

interface AddPlayerModalProps {
    visible: boolean;
    onClose: () => void;
    onSubmit: (player: Player) => void;
}

const AddPlayerModal: React.FC<AddPlayerModalProps> = ({ visible, onClose, onSubmit }) => {
    const [name, setName] = useState('');
    const [position, setPosition] = useState('');
    const [team, setTeam] = useState('');
    const currentYear = new Date().getFullYear();
    const [draftyear, setDraftyear] = useState<number>(currentYear);

    const handleSubmit = () => {
        if (!name || !position || !team) return;

        onSubmit({
            name,
            position,
            team,
            draftyear,
            adp: -1
        });

        setName('');
        setPosition('');
        setTeam('');
        setDraftyear(currentYear);
        onClose();
    };

    if (!visible) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <h2>Add Player</h2>
                <input placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} />

                <select value={position} onChange={(e) => setPosition(e.target.value)}>
                    <option value="">Select Position</option>
                    <option value="QB">QB</option>
                    <option value="RB">RB</option>
                    <option value="WR">WR</option>
                    <option value="TE">TE</option>
                </select>

                <input placeholder="Team" value={team} onChange={(e) => setTeam(e.target.value)} />

                <label className="slider-label">
                    Year: <strong>{draftyear}</strong>
                </label>
                <input
                    type="range"
                    min={currentYear}
                    max={currentYear + 5}
                    value={draftyear}
                    onChange={(e) => setDraftyear(Number(e.target.value))}
                />

                <button onClick={handleSubmit}>Add</button>
                <button className="cancel" onClick={onClose}>Cancel</button>
            </div>
        </div>
    );
};

export default AddPlayerModal;
