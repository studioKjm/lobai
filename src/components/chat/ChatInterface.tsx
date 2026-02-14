import React, { useRef, useEffect, useState } from 'react';
import { Message } from '@/types';
import { useChatStore } from '@/stores/chatStore';
import { PersonaModal } from './PersonaModal';
import toast from 'react-hot-toast';

interface ChatInterfaceProps {
  messages: Message[];
  inputValue: string;
  isTyping: boolean;
  onInputChange: (value: string) => void;
  onSendMessage: () => void;
  onClearHistory?: () => void;
  isExpanded?: boolean;
  onToggleExpand?: () => void;
}

export const ChatInterface: React.FC<ChatInterfaceProps> = ({
  messages,
  inputValue,
  isTyping,
  onInputChange,
  onSendMessage,
  onClearHistory,
  isExpanded = false,
  onToggleExpand
}) => {
  const chatEndRef = useRef<HTMLDivElement>(null);
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { currentPersona, sendMessageWithFile, isStreaming, streamingContent } = useChatStore();
  const [isPersonaModalOpen, setIsPersonaModalOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    // Scroll chat container to bottom (without scrolling the entire page)
    if ((messages.length > 1 || streamingContent) && chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  }, [messages, streamingContent]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.nativeEvent.isComposing) {
      handleSend();
    }
  };

  const handleSend = () => {
    if (selectedFile) {
      sendMessageWithFile(inputValue, selectedFile);
      setSelectedFile(null);
      onInputChange('');
    } else {
      onSendMessage();
    }
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // Validate file size (10MB limit)
      if (file.size > 10 * 1024 * 1024) {
        toast.error('파일 크기는 10MB 이하여야 합니다');
        return;
      }

      // Validate file type (images and common documents)
      const allowedTypes = [
        'image/jpeg', 'image/png', 'image/gif', 'image/webp',
        'application/pdf', 'text/plain',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      ];

      if (!allowedTypes.includes(file.type)) {
        toast.error('지원하지 않는 파일 형식입니다');
        return;
      }

      setSelectedFile(file);
      toast.success(`${file.name} 선택됨`);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);

    const file = e.dataTransfer.files[0];
    if (file) {
      // Same validation as handleFileSelect
      if (file.size > 10 * 1024 * 1024) {
        toast.error('파일 크기는 10MB 이하여야 합니다');
        return;
      }

      const allowedTypes = [
        'image/jpeg', 'image/png', 'image/gif', 'image/webp',
        'application/pdf', 'text/plain',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      ];

      if (!allowedTypes.includes(file.type)) {
        toast.error('지원하지 않는 파일 형식입니다');
        return;
      }

      setSelectedFile(file);
      toast.success(`${file.name} 선택됨`);
    }
  };

  const handleRemoveFile = () => {
    setSelectedFile(null);
  };

  return (
    <>
      <div className={`transition-all duration-300 ${
        isExpanded
          ? 'h-[600px] lg:h-[750px]'
          : 'h-[300px] lg:h-[500px]'
      } glass flex flex-col overflow-hidden`}>
        <div className="p-4 border-b border-white/5 flex items-center justify-between">
          {/* Persona Display - Clickable */}
          <button
            onClick={() => setIsPersonaModalOpen(true)}
            className="flex items-center gap-3 hover:bg-white/5 rounded-lg px-3 py-2 -ml-3 transition-colors group"
          >
            <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
            <span className="text-2xl">{currentPersona?.iconEmoji || '🤖'}</span>
            <div className="text-left">
              <div className="text-xs font-semibold text-white/90 group-hover:text-white transition-colors">
                {currentPersona?.displayName || '로비 마스터'}
              </div>
              <div className="text-[10px] text-white/40">
                모드 변경
              </div>
            </div>
          </button>

          <div className="flex items-center gap-2">
            {/* Expand/Collapse button */}
            {onToggleExpand && (
              <button
                onClick={onToggleExpand}
                className="text-xs text-white/40 hover:text-cyan-400 transition-colors p-1"
                title={isExpanded ? "축소" : "확대"}
              >
                {isExpanded ? (
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                ) : (
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
                  </svg>
                )}
              </button>
            )}

            {/* Clear history button */}
            {onClearHistory && (
              <button
                onClick={onClearHistory}
                className="text-xs text-white/40 hover:text-red-400 transition-colors"
                title="대화 기록 초기화"
              >
                초기화
              </button>
            )}
          </div>
        </div>

        <div
          ref={chatContainerRef}
          className={`flex-1 overflow-y-auto p-6 space-y-4 ${isDragging ? 'bg-blue-500/10 border-2 border-dashed border-blue-500/50' : ''}`}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
        >
          {messages.map((msg, idx) => (
            <div key={idx} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-[85%] px-4 py-3 rounded-2xl text-sm leading-relaxed ${
                msg.role === 'user'
                ? 'bg-blue-600 text-white rounded-tr-none'
                : 'bg-white/5 text-white/90 rounded-tl-none border border-white/5'
              }`}>
                {msg.attachmentUrl && msg.attachmentType?.startsWith('image/') && (
                  <img
                    src={`http://localhost:8080/uploads/${msg.attachmentUrl}`}
                    alt={msg.attachmentName || '첨부 이미지'}
                    className="max-w-full rounded-lg mb-2"
                  />
                )}
                {msg.attachmentName && !msg.attachmentType?.startsWith('image/') && (
                  <div className="flex items-center gap-2 mb-2 text-xs opacity-60">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                    </svg>
                    {msg.attachmentName}
                  </div>
                )}
                {msg.content}
              </div>
            </div>
          ))}
          {isStreaming && streamingContent && (
            <div className="flex justify-start">
              <div className="max-w-[85%] px-4 py-3 rounded-2xl rounded-tl-none bg-white/5 text-white/90 border border-white/5 text-sm leading-relaxed">
                {streamingContent}
                <span className="inline-block w-1.5 h-4 bg-cyan-400/60 animate-pulse ml-0.5 align-text-bottom" />
              </div>
            </div>
          )}
          {isTyping && !isStreaming && (
            <div className="flex justify-start">
              <div className="bg-white/5 px-4 py-3 rounded-2xl rounded-tl-none border border-white/5">
                <div className="flex gap-1">
                  <div className="w-1.5 h-1.5 bg-white/40 rounded-full animate-bounce" />
                  <div className="w-1.5 h-1.5 bg-white/40 rounded-full animate-bounce [animation-delay:0.2s]" />
                  <div className="w-1.5 h-1.5 bg-white/40 rounded-full animate-bounce [animation-delay:0.4s]" />
                </div>
              </div>
            </div>
          )}
          <div ref={chatEndRef} />
        </div>

        <div className="p-4 bg-white/5 border-t border-white/5">
          {/* Selected file preview */}
          {selectedFile && (
            <div className="mb-3 flex items-center gap-2 p-2 bg-blue-500/20 rounded-lg">
              {selectedFile.type.startsWith('image/') ? (
                <img
                  src={URL.createObjectURL(selectedFile)}
                  alt="Preview"
                  className="w-12 h-12 object-cover rounded"
                />
              ) : (
                <div className="w-12 h-12 bg-white/10 rounded flex items-center justify-center">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
              )}
              <div className="flex-1 text-xs">
                <div className="font-medium">{selectedFile.name}</div>
                <div className="text-white/40">{(selectedFile.size / 1024).toFixed(1)} KB</div>
              </div>
              <button
                onClick={handleRemoveFile}
                className="p-1 hover:bg-white/10 rounded transition-colors"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          )}

          {/* Input area */}
          <div className="flex gap-2">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*,.pdf,.txt,.doc,.docx"
              onChange={handleFileSelect}
              className="hidden"
            />
            <button
              onClick={() => fileInputRef.current?.click()}
              className="p-2 hover:bg-white/10 rounded-xl transition-colors opacity-60 hover:opacity-100"
              title="파일 첨부"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/>
              </svg>
            </button>
            <input
              type="text"
              value={inputValue}
              onChange={(e) => onInputChange(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder={selectedFile ? "파일과 함께 보낼 메시지..." : "Lobi에게 메시지를 보내세요..."}
              className="flex-1 bg-transparent border-none outline-none text-sm placeholder:opacity-30"
            />
            <button
              onClick={handleSend}
              className="p-2 hover:bg-white/10 rounded-xl transition-colors opacity-60 hover:opacity-100"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="m22 2-7 20-4-9-9-4Z"/>
                <path d="M22 2 11 13"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      {/* Persona Modal - Outside main container */}
      <PersonaModal
        isOpen={isPersonaModalOpen}
        onClose={() => setIsPersonaModalOpen(false)}
      />
    </>
  );
};
