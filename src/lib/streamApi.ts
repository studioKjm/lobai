/**
 * SSE 스트리밍 API 클라이언트
 *
 * fetch + ReadableStream으로 SSE를 파싱하여
 * 실시간 응답 청크를 콜백으로 전달한다.
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export interface StreamChunk {
  content: string;
  done: boolean;
}

export function streamMessage(
  content: string,
  personaId: number,
  onChunk: (text: string) => void,
  onDone: () => void,
  onError: (error: Error) => void
): AbortController {
  const abortController = new AbortController();

  const token = localStorage.getItem('accessToken');

  fetch(`${API_BASE_URL}/messages/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ content, personaId }),
    signal: abortController.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('ReadableStream not supported');
      }

      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });

        // SSE 파싱: 줄 단위로 처리
        const lines = buffer.split('\n');
        buffer = lines.pop() || ''; // 마지막 불완전한 줄은 버퍼에 유지

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const jsonStr = line.substring(5).trim();
            if (!jsonStr) continue;

            try {
              const chunk: StreamChunk = JSON.parse(jsonStr);
              if (chunk.done) {
                onDone();
                return;
              }
              if (chunk.content) {
                onChunk(chunk.content);
              }
            } catch {
              // JSON 파싱 실패 시 무시
            }
          } else if (line.startsWith('event:done')) {
            onDone();
            return;
          } else if (line.startsWith('event:error')) {
            // 다음 data 줄에서 에러 메시지 읽기
          }
        }
      }

      // 스트림 종료 (done 이벤트 없이)
      onDone();
    })
    .catch((error) => {
      if (error.name === 'AbortError') {
        return; // 사용자가 취소
      }
      onError(error);
    });

  return abortController;
}
