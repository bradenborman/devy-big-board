import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import './bubbleMenu.scss';

interface BubbleMenuProps {
    onClearBoard: () => void;
    onExportDraft: () => void;
    onLastPlayerRemove: () => void;
    onAddPlayerClick: () => void;
    onAddTierBreak: () => void;
    onRemoveLastTierBreak: () => void;
    onResetDraft: () => void;
    isBoardPopulated: boolean;
    playerListOpen: boolean;
    setPlayerListOpen: (open: boolean) => void;
}

const BubbleMenu: React.FC<BubbleMenuProps> = ({
    onClearBoard,
    onExportDraft,
    onLastPlayerRemove,
    onAddPlayerClick,
    onAddTierBreak,
    onRemoveLastTierBreak,
    onResetDraft,
    isBoardPopulated,
    playerListOpen,
    setPlayerListOpen
}) => {
    const [open, setOpen] = useState(false);
    const navigate = useNavigate();

    if (window.innerWidth >= 768) {
        return null;
    }

    return (
        <div className="bubble-menu-wrapper">
            <button className="bubble-toggle" onClick={() => setOpen(prev => !prev)}>
                {open ? '×' : '☰'}
            </button>

            {open && (
                <div className="bubble-menu">
                    <button onClick={isBoardPopulated ? onClearBoard : undefined}>Clear</button>
                    <button onClick={isBoardPopulated ? onExportDraft : undefined}>Export</button>
                    <button onClick={isBoardPopulated ? onLastPlayerRemove : undefined}>Undo Pick</button>
                    <button onClick={onAddPlayerClick}>Add Player</button>
                    <button onClick={onAddTierBreak}>Tier Break</button>
                    <button onClick={onRemoveLastTierBreak}>Undo Break</button>
                    <button onClick={() => {
                        onResetDraft();
                        navigate('/');
                    }}>
                        New Draft
                    </button>
                    <button onClick={() => setPlayerListOpen(!playerListOpen)}>
                        {playerListOpen ? "Hide Player List" : "Show Player List"}
                    </button>
                </div>
            )}
        </div>
    );
};

export default BubbleMenu;
