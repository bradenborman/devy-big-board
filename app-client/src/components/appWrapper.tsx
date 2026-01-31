import React from 'react';
import { BrowserRouter, Routes, Route, useParams } from 'react-router-dom';
import HomePage from './pages/HomePage';
import MainComponent from './mainComponent';
import LiveDraftPage from './pages/LiveDraftPage';
import DraftSetupPage from './pages/DraftSetupPage';
import DraftLobbyPage from './pages/DraftLobbyPage';
import PlayerManagementPage from './pages/PlayerManagementPage';
import ConsentBanner from './shared/ConsentBanner';
import CompletedDraft from './pages/CompletedDraft';
import LiveDraftBoard from './draft/LiveDraftBoard';
import { WebSocketProvider } from '../contexts/WebSocketContext';

const AppWrapper: React.FC = () => {
    return (
        <BrowserRouter>
            <WebSocketProvider>
                <ConsentBanner />
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/offline-draft" element={
                        <div className="app-wrapper">
                            <MainComponent />
                        </div>
                    } />
                    <Route path="/live-draft" element={<LiveDraftPage />} />
                    <Route path="/live-draft/setup" element={<DraftSetupPage />} />
                    <Route path="/draft/:uuid/lobby" element={<DraftLobbyPage />} />
                    <Route path="/draft/:uuid" element={<DraftRoute />} />
                    <Route path="/player-management" element={<PlayerManagementPage />} />
                </Routes>
            </WebSocketProvider>
        </BrowserRouter>
    );
};

const DraftRoute: React.FC = () => {
    const { uuid } = useParams<{ uuid: string }>();
    const searchParams = new URLSearchParams(window.location.search);
    const position = searchParams.get('x');
    
    if (!uuid) return <div>Invalid Draft</div>;
    
    // If there's a position query param, it's a live draft
    if (position) {
        return <LiveDraftBoard />;
    }
    
    // Otherwise, it's a completed draft view
    return <CompletedDraft uuid={uuid} />
};

export default AppWrapper;