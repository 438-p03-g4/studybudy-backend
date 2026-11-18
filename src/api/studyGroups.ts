import { Platform } from 'react-native';
import type { StudyGroup } from '@/types/studyGroup';

const defaultApiBaseUrl = Platform.OS === 'android'
  ? 'http://10.0.2.2:8080'
  : 'http://localhost:8080';

const API_BASE_URL = (process.env.EXPO_PUBLIC_API_URL ?? defaultApiBaseUrl).replace(/\/$/, '');

type StudyGroupResponse = {
  id: number | string;
  name: string;
  course: string;
};

export async function fetchStudyGroups(
  query: string,
  signal?: AbortSignal,
): Promise<StudyGroup[]> {
  const params = new URLSearchParams();
  const trimmedQuery = query.trim();
  if (trimmedQuery) params.set('query', trimmedQuery);

  const queryString = params.toString();
  const response = await fetch(`${API_BASE_URL}/groups${queryString ? `?${queryString}` : ''}`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    signal,
  });

  if (!response.ok) {
    throw new Error(`Unable to load study groups (${response.status})`);
  }

  const data: unknown = await response.json();
  if (!Array.isArray(data)) throw new Error('The study groups response was invalid.');

  return data.filter(isStudyGroupResponse).map((group) => ({
    id: String(group.id),
    name: group.name,
    course: group.course,
  }));
}

function isStudyGroupResponse(value: unknown): value is StudyGroupResponse {
  if (!value || typeof value !== 'object') return false;
  const group = value as Record<string, unknown>;
  return (
    (typeof group.id === 'number' || typeof group.id === 'string') &&
    typeof group.name === 'string' &&
    typeof group.course === 'string'
  );
}
